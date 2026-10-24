package com.microservice.product.config;

import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/*
 * Point de départ de la configuration Spring (c'est la classe passée au conteneur dans MainApp).
 * @ComponentScan explore com.microservice.product et tous ses sous-paquets, et enregistre :
 *   - les DAO       (@Repository)  : ProductDaoImpl, CategorieDaoImpl
 *   - les services  (@Service)     : ProductServiceImpl, CategorieServiceImpl
 *   - les autres configurations (@Configuration) : HibernateConfig, TransactionConfig
 */
@Configuration
@ComponentScan(basePackages = "com.microservice.product")
public class AppConfig {
}
