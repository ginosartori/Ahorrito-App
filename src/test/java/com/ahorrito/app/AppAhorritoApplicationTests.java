package com.ahorrito.app;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class AppAhorritoApplicationTests {

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		try {
			Class<?> clazz = Class.forName("org.springframework.boot.autoconfigure.flyway.FlywayAutoConfiguration");
			System.out.println("====== FlywayAutoConfiguration found: " + clazz.getName() + " ======");
		} catch (ClassNotFoundException e) {
			System.out.println("====== FlywayAutoConfiguration NOT FOUND IN SPRING BOOT AUTOCONFIGURE ======");
		}
	}

	@Test
	void verifyDatabaseSchema() {
		// Fetch all table names in the current database schema
		List<String> tables = jdbcTemplate.queryForList(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = 'public'",
				String.class
		);
		
		System.out.println("====== TABLES FOUND IN DATABASE ======");
		tables.forEach(System.out::println);
		System.out.println("======================================");

		// Assert that the essential application tables exist
		assertTrue(tables.contains("usuarios"), "usuarios table should exist");
		assertTrue(tables.contains("categorias"), "categorias table should exist");
		assertTrue(tables.contains("carteras"), "carteras table should exist");
		assertTrue(tables.contains("transacciones"), "transacciones table should exist");
		assertTrue(tables.contains("eventos_calendario"), "eventos_calendario table should exist");
		assertTrue(tables.contains("gasto_checklist"), "gasto_checklist table should exist");
	}

}

