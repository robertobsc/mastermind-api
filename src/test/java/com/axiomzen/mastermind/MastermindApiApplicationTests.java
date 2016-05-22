package com.axiomzen.mastermind;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.axiomzen.mastermind.config.MastermindApiApplication;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = MastermindApiApplication.class)
@WebAppConfiguration
public class MastermindApiApplicationTests {

	@Test
	public void contextLoads() {
	}

}
