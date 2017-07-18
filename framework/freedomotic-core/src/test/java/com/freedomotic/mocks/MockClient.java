package com.freedomotic.mocks;

import com.freedomotic.api.Client;
import com.freedomotic.model.ds.Config;

public class MockClient implements Client, Mockable {

	private Config mockConfig;
	private String name = "MockName";
	
	@Override
	public void stop() {
		// TODO Auto-generated method stub
	}

	@Override
	public void start() {
		// TODO Auto-generated method stub
	}

	@Override
	public void showGui() {
		// TODO Auto-generated method stub
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public void setDescription(String description) {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean isRunning() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void hideGui() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getType() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Config getConfiguration() {
		return mockConfig;
	}

	@Override
	public void destroy() {
		// TODO Auto-generated method stub
	}

	@Override
	public Object getInstance() {
		return new MockClient();
	}
	
	public void setMockConfiguration(Config mockConfig) {
		this.mockConfig = mockConfig;
	}
}
