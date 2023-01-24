package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.LigneRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import jakarta.transaction.Transactional;
import jakarta.validation.constraints.Positive;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.validation.annotation.Validated;

@Service
@Validated // Les contraintes de validatipn des méthodes sont vérifiées
public class LigneService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final LigneRepository ligneDao;
    private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public LigneService(CommandeRepository commandeDao, LigneRepository ligneDao, ProduitRepository produitDao) {
        this.commandeDao = commandeDao;
        this.ligneDao = ligneDao;
        this.produitDao = produitDao;
    }

    /**
     * <pre>
     * Service métier : 
     *     Enregistre une nouvelle ligne de commande pour une commande connue par sa clé,
     *     Incrémente la quantité totale commandée (Produit.unitesCommandees) avec la quantite à commander
     * Règles métier :
     *     - le produit référencé doit exister
     *     - la commande doit exister
     *     - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     *     - la quantité doit être positive
     *     - On doit avoir une quantite en stock du produit suffisante
     * <pre>
     * 
     *  @param commandeNum la clé de la commande
     *  @param produitRef la clé du produit
     *  @param quantite la quantité commandée (positive)
     *  @return la ligne de commande créée
     */
    @Transactional
    Ligne ajouterLigne(Integer commandeNum, Integer produitRef, @Positive int quantite) {
        // On vérifie que le produit existe
        var produit = produitDao.findById(produitRef).orElseThrow();
        // On vérifie que la commande existe
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        // On récupère le nombre de produit restant en stock
        var resteEnStock = produit.getUnitesEnStock();
        // On crée une nouvelle ligne de commande
        var ligne = new Ligne();
        // On vérifie que la commande n'est pas encore envoyée
        if( commande.getEnvoyeele() == null ) {
            // On vérifie que la quantité à commander est positive
            // On vérifie qu'il y a assez de produit en stock pour prendre en compte la commande
            if( quantite > 0 && resteEnStock >= quantite ){
                ligne.setCommande(commande);
                ligne.setProduit(produit);
                ligne.setQuantite(quantite);
                // Enregistrer une nouvelle ligne dans la base de données
                ligneDao.save(ligne); // Ajout d'une nouvelle ligne "save" pour insérer une nouvelle ligne
                // Mettre à jour le nombre de produit commandé
                produit.setUnitesCommandees(produit.getUnitesCommandees() + quantite);
            } else {
                throw new IllegalArgumentException("La quantité doit être positive ou inférieure aux nombre de produit en stock");
            }
        }else {
            throw new DataIntegrityViolationException("On ne peut pas ajouter une ligne à une commande déjà envoyée");
        }
        return ligne;
    }
}