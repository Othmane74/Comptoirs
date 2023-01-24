package comptoirs.service;

import java.math.BigDecimal;
import java.time.LocalDate;

import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import comptoirs.dao.ClientRepository;
import comptoirs.dao.CommandeRepository;
import comptoirs.entity.Commande;
import jakarta.transaction.Transactional;

@Service
public class CommandeService {
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    private final CommandeRepository commandeDao;
    private final ClientRepository clientDao;

    // private final ProduitRepository produitDao;

    // @Autowired
    // La couche "Service" utilise la couche "Accès aux données" pour effectuer les traitements
    public CommandeService(CommandeRepository commandeDao, ClientRepository clientDao /*, ProduitRepository produitDao*/) {
        this.commandeDao = commandeDao;
        this.clientDao = clientDao;
        // this.produitDao = produitDao;
    }
    /**
     * Service métier : Enregistre une nouvelle commande pour un client connu par sa clé
     * Règles métier :
     * - le client doit exister
     * - On initialise l'adresse de livraison avec l'adresse du client
     * - Si le client a déjà commandé plus de 100 articles, on lui offre une remise de 15%
     * @param clientCode la clé du client
     * @return la commande créée
     */
    @Transactional
    public Commande creerCommande(String clientCode) {
        // On vérifie que le client existe
        var client = clientDao.findById(clientCode).orElseThrow();
        // On crée une commande pour ce client
        var nouvelleCommande = new Commande(client);
        // On initialise l'adresse de livraison avec l'adresse du client
        nouvelleCommande.setAdresseLivraison(client.getAdresse());
        // Si le client a déjà commandé plus de 100 articles, on lui offre une remise de 15%
        // La requête SQL nécessaire est définie dans l'interface ClientRepository
        var nbArticles = clientDao.nombreArticlesCommandesPar(clientCode);
        if (nbArticles > 100) {
            nouvelleCommande.setRemise(new BigDecimal("0.15"));
        }
        // On enregistre la commande (génère la clé)
        commandeDao.save(nouvelleCommande);
        return nouvelleCommande;
    }

    /**
     * Service métier : Enregistre l'expédition d'une commande connue par sa clé
     * Règles métier :
     * - la commande doit exister
     * - la commande ne doit pas être déjà envoyée (le champ 'envoyeele' doit être null)
     * - On met à jour la date d'expédition (envoyeele) avec la date du jour
     * - Pour chaque produit commandé, décrémente la quantité en stock (Produit.unitesEnStock)
     *   de la quantité commandée
     * @param commandeNum la clé de la commande
     * @return la commande mise à jour
     */
    @Transactional
    public Commande enregistreExpedition(Integer commandeNum) {
        // On vérifie si la commande existe
        var commande = commandeDao.findById(commandeNum).orElseThrow();
        // On vérifie si la commande n'est pas encore envoyée
        var estEnvoyee = commande.getEnvoyeele();
        // Si la commande n'est pas encore envoyé, on définit la date d'envoi comme la date du jour
        if(estEnvoyee == null){
            commande.setEnvoyeele(LocalDate.now());
        }else {
            throw new DataIntegrityViolationException("Cette commande a été déjà livrée");
        }
        // On récupère les lignes de la commande
        var lignesDeCommande = commande.getLignes();
        for(Ligne ligne : lignesDeCommande) {
            // On récupère chaque produit
            var produit = ligne.getProduit();
            // On décréménte la quantité en stock du produit de la quantité commandé
            produit.setUnitesEnStock(produit.getUnitesEnStock() - ligne.getQuantite());
            // On met à effet la modification du produit dans la base de donnée
            // produitDao.save(produit); // pas besoin d'appeler save dans un serice annoté transactional
        }
        // On met à effet la modification de la commande dans la base de donnée
        return commande;
    }
}