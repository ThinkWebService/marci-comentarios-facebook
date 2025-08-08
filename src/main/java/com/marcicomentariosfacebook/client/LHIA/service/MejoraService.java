package com.marcicomentariosfacebook.client.LHIA.service;

import reactor.core.publisher.Flux;

public interface MejoraService {

	Flux<String> sendMesssageToMejora(String message);
}