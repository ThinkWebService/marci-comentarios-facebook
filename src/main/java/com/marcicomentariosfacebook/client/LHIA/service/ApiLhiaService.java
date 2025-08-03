package com.marcicomentariosfacebook.client.LHIA.service;

import reactor.core.publisher.Mono;

public interface ApiLhiaService {

	Mono<String> sendMesssageToLhia(String message);

	Mono<String> getToken();

}
