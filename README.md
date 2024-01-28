# One Time Password Generator

## Setting-up the project:
* Set project SDK: Java 17.0.9 Amazon Corretto
* Install/Enable Lombok plugin in your IDE
* For IntelliJ - Enable annotation processing
* >docker-compose up -d

NOTE: only MySQL and a GUI are managed through Docker, since the application wasn't able to connect to MySQL within the 
Docker network

* Compile and run the application: 
>mvn spring-boot:run -Dspring-boot.run.profiles=docker-compose
 
## Sending the OTP request
* >curl -X POST http://localhost:8080/otp/create -H "Content-Type: application/x-www-form-urlencoded" -d "email=value1"

## Obtaining the One Time Password:    
To access the OTP from an email inbox, go to the test inbox at
https://www.wpoven.com/tools/free-smtp-server-for-testing and enter the same email used for generating the One Time Password

## Verifying the OTP
* >curl -X POST http://localhost:8080/otp/validate -H "Content-Type: application/x-www-form-urlencoded" -d "email=asdf@asdf.com&otp=041069"