# MonNes_Server
## Giới thiệu
Đây là project backend sử dụng Spring Boot và Maven để xây dựng ứng dụng web RESTful API.

## Công nghệ sử dụng
- Java 21 
- Spring Boot 3.4.5
- Maven
- Spring Data JPA
- Spring Security 
- MySQL 
- Lombok 

## Cài đặt và chạy project

### Yêu cầu
- JDK 21 trở lên đã được cài đặt
- Maven đã được cài đặt
- Database MySQL đã được cấu hình và chạy

### Các bước

1. Clone repository
```bash
git clone https://github.com/hathuanihi/MonNes_Server.git
```
2. Cấu hình application.properties
```bash
# MySQL
spring.datasource.url=jdbc:mysql://localhost:3306/quan_ly_so_tiet_kiem_db?useUnicode=true&characterEncoding=UTF-8
spring.datasource.username=root
spring.datasource.password=<your-password>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

# Encoding
server.servlet.encoding.charset=UTF-8
server.servlet.encoding.enabled=true
server.servlet.encoding.force=true

# SQL Initialization
spring.sql.init.mode=always
spring.jpa.defer-datasource-initialization=true
spring.sql.init.continue-on-error=true

# Server
server.port=8080

# Swagger
springdoc.api-docs.path=/api-docs
springdoc.swagger-ui.path=/swagger-ui.html
springdoc.swagger-ui.operationsSorter=method

# Email 
spring.mail.host=smtp.gmail.com
spring.mail.port=587
spring.mail.username=23521544@gm.uit.edu.vn
spring.mail.password=ckvf sjyl fddm tssb
spring.mail.properties.mail.smtp.auth=true
spring.mail.properties.mail.smtp.starttls.enable=true

# Logging
logging.level.org.springframework.security=DEBUG
logging.level.com.SE104.quan_ly_so_tiet_kiem=DEBUG
logging.level.org.springframework.security.access=TRACE
logging.level.org.springframework.security.web=TRACE
logging.level.org.springframework.web=TRACE

# JWT Settings
jwt.secret=DXhrYHEWV57PqRAI2Ban96EHOcWIoAiB0J2F+nsI4HA=
jwt.expiration=86400000 
```
3. Build project
```bash
mvn install
```
4. Run project
```bash
mvn spring-boot:run
```
