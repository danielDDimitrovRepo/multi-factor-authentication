# One Time Password Generator

## Setting-up the project:
* Set project SDK: Java 17.0.9 Amazon Corretto
* Install/Enable Lombok plugin in your IDE
* For IntelliJ - Enable annotation processing
* >mvn package
* >docker image build -t multi-factor-authentication:latest .
* >docker-compose up -d
 
## Sending the OTP request
* >curl -X POST http://localhost:8080/otp/create -H "Content-Type: application/x-www-form-urlencoded" -d "email=[email]"

## Obtaining the One Time Password:    
* To access the OTP from an email inbox, go to the test inbox at http://localhost:8082/

## Verifying the OTP
* >curl -X POST http://localhost:8080/otp/validate -H "Content-Type: application/x-www-form-urlencoded" -d "email=[email]&otp=[otp]"
 
## Inspecting the Database
* You can inspect at any time at http://localhost:8081/ - user: root, pass: password