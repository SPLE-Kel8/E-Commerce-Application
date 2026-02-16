package com.app.config;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.app.entites.Address;
import com.app.entites.Cart;
import com.app.entites.Category;
import com.app.entites.Product;
import com.app.entites.Role;
import com.app.entites.StoreDiscount;
import com.app.entites.User;
import com.app.repositories.CategoryRepo;
import com.app.repositories.ProductRepo;
import com.app.repositories.RoleRepo;
import com.app.repositories.StoreDiscountRepo;
import com.app.repositories.UserRepo;

@Component
public class DataSeeder {

	@Autowired
	private RoleRepo roleRepo;

	@Autowired
	private UserRepo userRepo;

	@Autowired
	private CategoryRepo categoryRepo;

	@Autowired
	private ProductRepo productRepo;

	// === VAR-3: Store Discount ===
	@Autowired
	private StoreDiscountRepo storeDiscountRepo;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Transactional
	public void seed() {
		// ===== 1. Seed Roles =====
		Role adminRole = new Role();
		adminRole.setRoleId(AppConstants.ADMIN_ID);
		adminRole.setRoleName("ADMIN");

		Role userRole = new Role();
		userRole.setRoleId(AppConstants.USER_ID);
		userRole.setRoleName("USER");

		roleRepo.saveAll(List.of(adminRole, userRole));

		// Re-fetch roles within same transaction so they are managed
		Role managedAdminRole = roleRepo.findById(AppConstants.ADMIN_ID).get();
		Role managedUserRole = roleRepo.findById(AppConstants.USER_ID).get();
		System.out.println(managedAdminRole);
		System.out.println(managedUserRole);

		// ===== 2. Seed Admin User =====
		if (userRepo.findByEmail("admin@webshop.com").isEmpty()) {
			Address adminAddress = new Address("Indonesia", "DKI Jakarta", "Jakarta", "101010",
					"Jalan Sudirman", "Gedung Admin Tower");

			User admin = new User();
			admin.setFirstName("AdminWebshop");
			admin.setLastName("SuperAdmin");
			admin.setMobileNumber("0811111111");
			admin.setEmail("admin@webshop.com");
			admin.setPassword(passwordEncoder.encode("admin123"));

			Set<Role> adminRoles = new HashSet<>();
			adminRoles.add(managedAdminRole);
			adminRoles.add(managedUserRole);
			admin.setRoles(adminRoles);
			admin.setAddresses(List.of(adminAddress));

			Cart adminCart = new Cart();
			adminCart.setUser(admin);
			admin.setCart(adminCart);

			userRepo.save(admin);
			System.out.println("Admin user seeded: admin@webshop.com / admin123");
		}

		// ===== 3. Seed Regular User =====
		if (userRepo.findByEmail("user@webshop.com").isEmpty()) {
			Address userAddress = new Address("Indonesia", "Jawa Barat", "Depok", "164242",
					"Jalan Margonda Raya", "Building Fasilkom");

			User regularUser = new User();
			regularUser.setFirstName("RegularUser");
			regularUser.setLastName("Pembeli");
			regularUser.setMobileNumber("0822222222");
			regularUser.setEmail("user@webshop.com");
			regularUser.setPassword(passwordEncoder.encode("user1234"));

			Set<Role> userRoles = new HashSet<>();
			userRoles.add(managedUserRole);
			regularUser.setRoles(userRoles);
			regularUser.setAddresses(List.of(userAddress));

			Cart userCart = new Cart();
			userCart.setUser(regularUser);
			regularUser.setCart(userCart);

			userRepo.save(regularUser);
			System.out.println("Regular user seeded: user@webshop.com / user1234");
		}

		// ===== 4. Seed Categories =====
		if (categoryRepo.count() == 0) {
			Category electronics = new Category();
			electronics.setCategoryName("Electronics");

			Category clothing = new Category();
			clothing.setCategoryName("Clothing");

			Category books = new Category();
			books.setCategoryName("Books");

			Category homeAppliances = new Category();
			homeAppliances.setCategoryName("Home Appliances");

			Category sports = new Category();
			sports.setCategoryName("Sports & Outdoors");

			List<Category> savedCategories = categoryRepo.saveAll(
					List.of(electronics, clothing, books, homeAppliances, sports));
			savedCategories.forEach(c -> System.out.println("Category seeded: " + c.getCategoryName()));

			// ===== 5. Seed Products =====
			List<Product> products = List.of(
					// Electronics
					createProduct("Smartphone Samsung", "default.png",
							"Latest Samsung Galaxy smartphone with 128GB storage", 15, 7999000, 10,
							savedCategories.get(0)),
					createProduct("Wireless Earbuds", "default.png",
							"Bluetooth 5.0 wireless earbuds with noise cancellation", 30, 599000, 5,
							savedCategories.get(0)),
					createProduct("Laptop Lenovo", "default.png",
							"Lenovo ThinkPad with Intel i7 and 16GB RAM", 10, 12999000, 8,
							savedCategories.get(0)),

					// Clothing
					createProduct("Kaos Polos Premium", "default.png",
							"Cotton combed 30s premium quality t-shirt", 100, 89000, 15,
							savedCategories.get(1)),
					createProduct("Celana Jeans Slim", "default.png",
							"Slim fit denim jeans with stretch fabric", 50, 259000, 10,
							savedCategories.get(1)),
					createProduct("Jaket Hoodie", "default.png",
							"Fleece hoodie jacket with front zipper", 40, 199000, 20,
							savedCategories.get(1)),

					// Books
					createProduct("Clean Code Book", "default.png",
							"A Handbook of Agile Software Craftsmanship by Robert C. Martin", 25, 350000, 5,
							savedCategories.get(2)),
					createProduct("Design Patterns", "default.png",
							"Elements of Reusable Object-Oriented Software by Gang of Four", 20, 420000, 10,
							savedCategories.get(2)),

					// Home Appliances
					createProduct("Rice Cooker Digital", "default.png",
							"Digital rice cooker with 1.8L capacity and timer", 20, 450000, 12,
							savedCategories.get(3)),
					createProduct("Blender Portable", "default.png",
							"USB rechargeable portable blender for smoothies", 35, 189000, 15,
							savedCategories.get(3)),

					// Sports & Outdoors
					createProduct("Yoga Mat Premium", "default.png",
							"Non-slip yoga mat with 6mm thickness", 60, 150000, 10,
							savedCategories.get(4)),
					createProduct("Resistance Bands Set", "default.png",
							"Set of 5 resistance bands for home workout", 45, 120000, 5,
							savedCategories.get(4)));

			List<Product> savedProducts = productRepo.saveAll(products);
			savedProducts.forEach(p -> System.out.println("Product seeded: " + p.getProductName()));
		}

		// ===== 6. Seed Store Discounts (VAR-3) =====
		if (storeDiscountRepo.count() == 0) {
			StoreDiscount newYearSale = new StoreDiscount();
			newYearSale.setDiscountName("New Year Sale");
			newYearSale.setDiscountPercentage(10.0);
			newYearSale.setMinOrderAmount(500000.0);
			newYearSale.setActive(true);
			newYearSale.setStartDate(java.time.LocalDate.now());
			newYearSale.setEndDate(java.time.LocalDate.now().plusMonths(3));

			StoreDiscount megaSale = new StoreDiscount();
			megaSale.setDiscountName("Mega Sale");
			megaSale.setDiscountPercentage(15.0);
			megaSale.setMinOrderAmount(1000000.0);
			megaSale.setActive(true);
			megaSale.setStartDate(java.time.LocalDate.now());
			megaSale.setEndDate(java.time.LocalDate.now().plusMonths(1));

			StoreDiscount flashDeal = new StoreDiscount();
			flashDeal.setDiscountName("Flash Deal");
			flashDeal.setDiscountPercentage(5.0);
			flashDeal.setMinOrderAmount(100000.0);
			flashDeal.setActive(true);
			flashDeal.setStartDate(java.time.LocalDate.now());
			flashDeal.setEndDate(java.time.LocalDate.now().plusWeeks(2));

			List<StoreDiscount> savedDiscounts = storeDiscountRepo
					.saveAll(List.of(newYearSale, megaSale, flashDeal));
			savedDiscounts.forEach(
					d -> System.out.println("Store discount seeded: " + d.getDiscountName() + " (" + d.getDiscountPercentage() + "%)"));
		}

		System.out.println("========================================");
		System.out.println("Data seeding completed successfully!");
		System.out.println("========================================");
		System.out.println("Admin login -> email: admin@webshop.com | password: admin123");
		System.out.println("User  login -> email: user@webshop.com  | password: user1234");
		System.out.println("========================================");
	}

	private Product createProduct(String name, String image, String description, int quantity, double price,
			double discount, Category category) {
		Product product = new Product();
		product.setProductName(name);
		product.setImage(image);
		product.setDescription(description);
		product.setQuantity(quantity);
		product.setPrice(price);
		product.setDiscount(discount);
		product.setSpecialPrice(price - (price * discount / 100));
		product.setCategory(category);
		return product;
	}
}
