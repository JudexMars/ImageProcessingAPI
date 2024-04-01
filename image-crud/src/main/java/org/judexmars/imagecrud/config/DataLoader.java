package org.judexmars.imagecrud.config;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.judexmars.imagecrud.model.PrivilegeEntity;
import org.judexmars.imagecrud.model.RoleEntity;
import org.judexmars.imagecrud.repository.PrivilegeRepository;
import org.judexmars.imagecrud.repository.RoleRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;

@Component
@RequiredArgsConstructor
public class DataLoader implements ApplicationRunner {

    private final PrivilegeRepository privilegeRepository;

    private final RoleRepository roleRepository;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        initPrivileges();
        initRoles();
    }

    private void initPrivileges() {

        var privileges = List.of(
                new PrivilegeEntity().setName("UPLOAD_IMAGE"),
                new PrivilegeEntity().setName("DOWNLOAD_IMAGE"),
                new PrivilegeEntity().setName("DELETE_IMAGE")
        );

        Predicate<String> condition = (String x) -> privilegeRepository.findByName(x).isEmpty();

        for (var privilege : privileges) {
            createIf(privilege, privilegeRepository, () -> condition.test(privilege.getName()));
        }
    }

    private void initRoles() {

        // Пока без админа из-за ненадобности

        var viewerPrivileges = List.of(
                privilegeRepository.findByName("UPLOAD_IMAGE").orElseThrow(),
                privilegeRepository.findByName("DOWNLOAD_IMAGE").orElseThrow(),
                privilegeRepository.findByName("DELETE_IMAGE").orElseThrow()
        );

        var roles = List.of(
                new RoleEntity().setName("ROLE_VIEWER").setPrivileges(viewerPrivileges)
        );

        Predicate<String> condition = (String x) -> roleRepository.findByName(x).isEmpty();

        for (var role : roles) {
            createIf(role, roleRepository, () -> condition.test(role.getName()));
        }
    }

    private <T> T createIf(T entity, JpaRepository<T, ?> repository, BooleanSupplier condition) {
        if (condition.getAsBoolean()) {
            return repository.save(entity);
        }
        return null;
    }

//    @Bean
//    public ApplicationListener<ServletWebServerInitializedEvent> downloadSwaggerFile() {
//        var restTemplate = new RestTemplate();
//        return args -> {
//            String url = "http://localhost:8080/api/v1/my-swagger.yaml";
//            String destinationFile = "./swagger.yaml";
//
//            ResponseEntity<byte[]> response = restTemplate.getForEntity(url, byte[].class);
//
//            if (response.getStatusCode().is2xxSuccessful()) {
//                byte[] responseBody = response.getBody();
//                try (ByteArrayInputStream inputStream = new ByteArrayInputStream(responseBody);
//                     BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);
//                     OutputStream outputStream = new FileOutputStream(new FileSystemResource(destinationFile).getFile())) {
//                    byte[] data = new byte[1024];
//                    int bytesRead;
//                    while ((bytesRead = bufferedInputStream.read(data, 0, 1024)) != -1) {
//                        outputStream.write(data, 0, bytesRead);
//                    }
//                    System.out.println("File downloaded successfully");
//                } catch (IOException e) {
//                    System.out.println("Error downloading file: " + e.getMessage());
//                }
//            } else {
//                System.out.println("Failed to download file, status code: " + response.getStatusCodeValue());
//            }
//        };
//    }
}
