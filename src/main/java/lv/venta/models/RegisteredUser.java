package lv.venta.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lv.venta.enums.UserType;

import javax.persistence.*;
import java.util.Collection;

@Entity
@Table(name = "users")
@Getter @Setter
@NoArgsConstructor @ToString
public class RegisteredUser
{
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column
	private long userId;

	@Column(nullable = false, length = 32)
	private String name;
	@Column(nullable = false, length = 32)
	private String surname;

	@Column(nullable = false, unique = true, length = 254)
	private String email;

	@Column(nullable = false, length = 64)
	private String password;

	private UserType type = UserType.User;

	@Column(length = 20)
	private String phone;

	@ManyToOne
	@JoinColumn(name = "role_id")
	private Role role;

	@OneToMany(mappedBy = "inquiryUser")
	@ToString.Exclude
	private Collection<Inquiry> inquiries;

	public RegisteredUser(String email, String password) {
		setEmail(email);
		setPassword(password);
	}

	// for filtering
	public RegisteredUser(String name, String surname, String email, String phone, UserType type) {
		this.name = name;
		this.surname = surname;
		this.email = email;
		this.type = type;
		this.phone = phone;
	}
}
