package comptoirs.service;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

import comptoirs.dao.CommandeRepository;
import comptoirs.dao.ProduitRepository;
import comptoirs.entity.Commande;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Optional;

@SpringBootTest
        // Ce test est basé sur le jeu de données dans "test_data.sql"
class LigneServiceTest {
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;

    static final int NUMERO_COMMANDE_INEXISTANTE  = 99997;
    static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;
    static final int REFERENCE_PRODUIT_DISPONIBLE_2 = 94;
    static final int REFERENCE_PRODUIT_DISPONIBLE_3 = 95;
    static final int REFERENCE_PRODUIT_DISPONIBLE_4 = 96;
    static final int REFERENCE_PRODUIT_INDISPONIBLE = 97;
    static final int REFERENCE_PRODUIT_INEXISTANT = 100;
    static final int UNITES_COMMANDEES_AVANT = 0;

    @Autowired
    LigneService service;

    @Autowired
    ProduitRepository produitDao;

    @Autowired
    CommandeRepository commandeDao;

    @Test
    void onPeutAjouterDesLignesSiPasLivre() {
        var ligne = service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 1);
        assertNotNull(ligne.getId(),
                "La ligne doit être enregistrée, sa clé générée");
    }

    @Test
    void laQuantiteEstPositive() {
        assertThrows(ConstraintViolationException.class,
                () -> service.ajouterLigne(NUMERO_COMMANDE_PAS_LIVREE, REFERENCE_PRODUIT_DISPONIBLE_1, 0),
                "La quantite d'une ligne doit être positive");
    }

    @Test
    public void testAjouterLigneProduitInexistant() {
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        Integer produitRef = REFERENCE_PRODUIT_INEXISTANT;
        int quantite = 1;
        assertEquals(Optional.empty(), produitDao.findById(produitRef));
        assertNotNull(commandeDao.findById(commandeNum));

        assertThrows(NoSuchElementException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite),
                "On peut pas ajouter un produit inexistant à une commande");
    }

    @Test
    public void testAjouterLigneCommandeInexistante() {
        Integer commandeNum = NUMERO_COMMANDE_INEXISTANTE;
        Integer produitRef = REFERENCE_PRODUIT_DISPONIBLE_1;
        int quantite = 1;
        assertEquals(Optional.empty(), commandeDao.findById(commandeNum));
        assertNotNull(produitDao.findById(produitRef));
        assertThrows(NoSuchElementException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite),
                "On ne peut pas ajouter une ligne à une commande inexistante");
    }

    @Test
    public void testAjouterLigneCommandeDejaEnvoyee() {
        Integer commandeNum = NUMERO_COMMANDE_DEJA_LIVREE;
        Integer produitRef = REFERENCE_PRODUIT_DISPONIBLE_1;
        int quantite = 1;
        assertThrows(DataIntegrityViolationException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite),
                "On peut pas ajouter une ligne de commande à une commande déjà livrée");
    }

    @Test
    public void testVerifierQuantiteProduitSuffisante() {
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        Integer produitRef = REFERENCE_PRODUIT_DISPONIBLE_1;
        int quantite = 110;
        assertThrows(IllegalArgumentException.class, () -> service.ajouterLigne(commandeNum, produitRef, quantite),
                "Pour une ligne de commande la quantité doit être inférieure ou égale aux nombre de produits en stock");
    }

    @Test
    public void testVerifierStockCommandePourProduit(){
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        Integer produitRef = REFERENCE_PRODUIT_DISPONIBLE_1;
        int quantite = 5;
        // On récupère le produit avant de l'ajouter à la ligne de commande
        var produit = produitDao.findById(produitRef).orElseThrow();
        int quantiteCommandee = produit.getUnitesCommandees() ;
        service.ajouterLigne(commandeNum, produitRef, quantite);
        // On récupère le produit après l'avoir ajouter à la ligne de commande
        produit = produitDao.findById(produitRef).orElseThrow();
        // On vérifie si la mise à jour du nombre d'unités commandée a été prise en compte
        assertEquals(quantiteCommandee + quantite, produit.getUnitesCommandees(),
                "Pour une nouvelle ligne de commande on incrémente le nombre de produits commandés de la quantité de produit commandé");
    }
    //hhhhhhh
}