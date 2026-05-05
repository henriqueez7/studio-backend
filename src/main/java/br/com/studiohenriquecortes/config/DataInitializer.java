package br.com.studiohenriquecortes.config;

import br.com.studiohenriquecortes.entity.BarbershopService;
import br.com.studiohenriquecortes.entity.Product;
import br.com.studiohenriquecortes.entity.User;
import br.com.studiohenriquecortes.enums.Role;
import br.com.studiohenriquecortes.repository.BarbershopServiceRepository;
import br.com.studiohenriquecortes.repository.ProductRepository;
import br.com.studiohenriquecortes.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.List;

@Configuration
@Profile("dev")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final BarbershopServiceRepository serviceRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.seed.admin.email:admin@studio.com}")
    private String adminEmail;

    @Value("${app.seed.admin.password:}")
    private String adminPassword;

    @Value("${app.seed.admin.name:Admin Studio}")
    private String adminName;

    @Value("${app.seed.admin.phone:(00) 00000-0000}")
    private String adminPhone;

    @Override
    public void run(String... args) {
        initializeAdminUser();
        initializeServices();
        initializeProducts();
    }

    private void initializeAdminUser() {
        if (adminPassword == null || adminPassword.isBlank()) {
            return;
        }

        if (userRepository.existsByEmail(adminEmail)) {
            return;
        }

        User adminUser = User.builder()
                .name(adminName)
                .email(adminEmail)
                .password(passwordEncoder.encode(adminPassword))
                .phone(adminPhone)
                .role(Role.ADMIN)
                .active(true)
                .build();

        userRepository.save(adminUser);
    }

    private void initializeServices() {
        List<BarbershopService> services = List.of(
                buildService("Degrade", "Corte com acabamento em degrade", "30.00", 40),
                buildService("Sobrancelha", "Modelagem e ajuste de sobrancelha", "20.00", 15),
                buildService("Barba", "Aparo, desenho e finalizacao da barba", "20.00", 20),
                buildService("Cavanhaque", "Ajuste e acabamento de cavanhaque", "10.00", 15),
                buildService("Botox Capilar", "Tratamento capilar para alinhamento e reducao de volume", "60.00", 60),
                buildService("Corte Kids", "Corte infantil com acabamento especial", "40.00", 35),
                buildService("Realinhamento", "Realinhamento capilar com acabamento", "60.00", 60),
                buildService("Hidratacao", "Tratamento de hidratacao capilar", "20.00", 20),
                buildService("Pigmentacao", "Pigmentacao capilar ou de barba", "20.00", 25),
                buildService("Corte Tesoura", "Corte feito exclusivamente na tesoura", "40.00", 45),
                buildService("Luzes", "Aplicacao de luzes no cabelo", "100.00", 90),
                buildService("Nevou", "Procedimento de descoloracao estilo nevou", "120.00", 120),
                buildService("Relaxamento Capilar", "Tratamento para relaxamento e controle dos fios", "40.00", 50)
        );

        services.stream()
                .filter(service -> !serviceRepository.existsByNameIgnoreCase(service.getName()))
                .forEach(serviceRepository::save);
    }

    private void initializeProducts() {
        List<Product> products = List.of(
                buildProduct("Pomada", "Pomada modeladora para finalizacao de penteados.", "Finalizacao", "18.00", "35.00", 20, 5),
                buildProduct("Balm", "Balm para hidratacao e modelagem da barba.", "Barba", "15.00", "30.00", 15, 4),
                buildProduct("Gel", "Gel fixador para cabelo com acabamento duradouro.", "Finalizacao", "10.00", "22.00", 18, 5),
                buildProduct("Shampoo", "Shampoo de uso profissional para limpeza capilar.", "Higiene", "16.00", "32.00", 12, 3),
                buildProduct("Condicionador", "Condicionador para hidratacao e maciez dos fios.", "Higiene", "17.00", "34.00", 10, 3)
        );

        products.stream()
                .filter(product -> isNewProduct(product.getName()))
                .forEach(productRepository::save);
    }

    private boolean isNewProduct(String name) {
        return productRepository.findAll()
                .stream()
                .noneMatch(product -> product.getName().equalsIgnoreCase(name));
    }

    private BarbershopService buildService(
            String name,
            String description,
            String price,
            Integer durationInMinutes
    ) {
        return BarbershopService.builder()
                .name(name)
                .description(description)
                .price(new BigDecimal(price))
                .durationInMinutes(durationInMinutes)
                .active(true)
                .build();
    }

    private Product buildProduct(
            String name,
            String description,
            String category,
            String purchasePrice,
            String salePrice,
            Integer stockQuantity,
            Integer minimumStock
    ) {
        return Product.builder()
                .name(name)
                .description(description)
                .category(category)
                .purchasePrice(new BigDecimal(purchasePrice))
                .salePrice(new BigDecimal(salePrice))
                .stockQuantity(stockQuantity)
                .minimumStock(minimumStock)
                .active(true)
                .build();
    }
}