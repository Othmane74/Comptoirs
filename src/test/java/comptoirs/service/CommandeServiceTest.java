package comptoirs.service;

import comptoirs.dao.CommandeRepository;
import comptoirs.entity.Ligne;
import comptoirs.entity.Produit;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
        // Ce test est basé sur le jeu de données dans "test_data.sql"
class CommandeServiceTest {
    private static final String ID_PETIT_CLIENT = "0COM";
    private static final String ID_GROS_CLIENT = "2COM";
    private static final String VILLE_PETIT_CLIENT = "Berlin";
    private static final BigDecimal REMISE_POUR_GROS_CLIENT = new BigDecimal("0.15");
    static final int NUMERO_COMMANDE_DEJA_LIVREE = 99999;
    static final int NUMERO_COMMANDE_PAS_LIVREE  = 99998;

    static final int NUMERO_COMMANDE_INEXISTANTE = 100000;

    static final int REFERENCE_PRODUIT_DISPONIBLE_1 = 93;

    @Autowired
    private CommandeService service;

    @Autowired
    CommandeRepository commandeDao;

    @Test
    void testCreerCommandePourGrosClient() {
        var commande = service.creerCommande(ID_GROS_CLIENT);
        assertNotNull(commande.getNumero(), "On doit avoir la clé de la commande");
        assertEquals(REMISE_POUR_GROS_CLIENT, commande.getRemise(),
                "Une remise de 15% doit être appliquée pour les gros clients");
    }

    @Test
    void testCreerCommandePourPetitClient() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertNotNull(commande.getNumero());
        assertEquals(BigDecimal.ZERO, commande.getRemise(),
                "Aucune remise ne doit être appliquée pour les petits clients");
    }

    @Test
    void testCreerCommandeInitialiseAdresseLivraison() {
        var commande = service.creerCommande(ID_PETIT_CLIENT);
        assertEquals(VILLE_PETIT_CLIENT, commande.getAdresseLivraison().getVille(),
                "On doit recopier l'adresse du client dans l'adresse de livraison");
    }

    @Test
    public void testExpedierCommandeInexistante() {
        Integer commandeNum = NUMERO_COMMANDE_INEXISTANTE;
        assertThrows(NoSuchElementException.class, () -> service.enregistreExpedition(commandeNum),
                "On ne peut pas expédier une commande inexistante");
    }


    @Test
    void testCommandeDejaLivree() {
        Integer commandeNum = NUMERO_COMMANDE_DEJA_LIVREE;
        assertThrows(DataIntegrityViolationException.class, () -> service.enregistreExpedition(commandeNum),
                "On ne peut pas livrée dont la date de livraison existe déjà");
    }

    @Test
    public void enregistreExpeditionDecrementeStock() {
        // Arrange
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        var commande = service.enregistreExpedition(commandeNum);
        for(Ligne ligne : commande.getLignes()){
            assertEquals(10, ligne.getProduit().getUnitesEnStock(),
                    "La quantité en stock doit être 10 conformément aux données de tests");
        }
    }

    @Test
    public void verifierDateExpedition() {
        Integer commandeNum = NUMERO_COMMANDE_PAS_LIVREE;
        var commande = service.enregistreExpedition(commandeNum);
        assertEquals(LocalDate.now(), commande.getEnvoyeele(),
                "Pour une commande non livrée , la date d'expédition de la commande doit correspondre à la date aujourd'hui");
    }

}
