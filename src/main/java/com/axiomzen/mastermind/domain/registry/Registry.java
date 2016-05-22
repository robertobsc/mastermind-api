package com.axiomzen.mastermind.domain.registry;

import java.util.Map;

import com.axiomzen.mastermind.domain.Entity;
import com.axiomzen.mastermind.utils.SecureKeyGenerator;

public abstract class Registry<T extends Entity> {

	public Registry() {
	}
	
	public abstract Map<String, T> getRegistryMap();
	
	private String generateKey() {
		return SecureKeyGenerator.generateKey();
	}
	
	public T create(T entity) {
		return save(entity);
	}
	
	public T save(T entity) {
		if (entity.getKey() == null || entity.getKey().isEmpty()) {
			entity.setKey(generateKey());
		}
		getRegistryMap().put(entity.getKey(), entity);
		
		return entity;
	}
	
	public T get(String key) {
		return getRegistryMap().get(key);
	}
	
	public void delete(String key){
		getRegistryMap().remove(key);
	}
}
