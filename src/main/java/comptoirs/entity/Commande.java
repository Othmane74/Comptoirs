package comptoirs.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Size;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@RequiredArgsConstructor
@ToString
public class Commande {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Basic(optional = false)
	@Column(nullable = false)
	@Setter(AccessLevel.NONE) // la clé est auto-générée par la BD, On ne veut pas de "setter"
	private Integer numero;

	@Basic(optional = false)
	@Column(nullable = false)
	@ToString.Exclude
	// Initialisée avec la date de création
	private LocalDate saisiele = LocalDate.now();

	@Basic(optional = true)
	@ToString.Exclude
	private LocalDate envoyeele = null;

	// @Max(value=?) @Min(value=?)//if you know range of your decimal fields
	// consider using these annotations to enforce field validation
	@Column(precision = 18, scale = 2)
	@ToString.Exclude
	private BigDecimal port = BigDecimal.ZERO;

	@Size(max = 40)
	@Column(length = 40)
	private String destinataire;

	@Embedded
	private AdressePostale adresseLivraison;

	@Basic(optional = false)
	@NonNull
	@Column(nullable = false, precision = 10, scale = 2)
	private BigDecimal remise = BigDecimal.ZERO;

	@OneToMany(cascade = CascadeType.ALL, mappedBy = "commande", orphanRemoval = true)
	private List<Ligne> lignes = new LinkedList<>();

	@ManyToOne(optional = false)
	@NonNull
	private Client client;

}