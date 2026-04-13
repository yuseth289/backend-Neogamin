package com.neogamin.proyecto_formativo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class ProyectoFormativoApplication {

	public static void main(String[] args) {
		SpringApplication.run(ProyectoFormativoApplication.class, args);
	}

}
