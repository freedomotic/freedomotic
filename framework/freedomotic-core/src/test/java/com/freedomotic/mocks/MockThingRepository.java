package com.freedomotic.mocks;

import java.io.File;
import java.util.List;

import com.freedomotic.environment.EnvironmentLogic;
import com.freedomotic.exceptions.RepositoryException;
import com.freedomotic.things.EnvObjectLogic;
import com.freedomotic.things.ThingRepository;

/**
 * Thing repository mock, for unit tests
 * @author P3trur0 https://flatmap.it
 *
 */
public class MockThingRepository implements ThingRepository, Mockable {
	
	@Override
	public List<EnvObjectLogic> findAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnvObjectLogic> findByName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnvObjectLogic findOne(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean create(EnvObjectLogic item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(EnvObjectLogic item) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean delete(String uuid) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public EnvObjectLogic modify(String uuid, EnvObjectLogic data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnvObjectLogic copy(EnvObjectLogic data) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void deleteAll() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public List<EnvObjectLogic> findByEnvironment(EnvironmentLogic env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnvObjectLogic> findByEnvironment(String uuid) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnvObjectLogic> findByProtocol(String protocolName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnvObjectLogic findByAddress(String protocol, String address) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public EnvObjectLogic load(File file) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<EnvObjectLogic> loadAll(File folder) throws RepositoryException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void saveAll(File folder) throws RepositoryException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Object getInstance() {
		return new MockThingRepository();
	}

}
