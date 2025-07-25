package com.marcicomentariosfacebook.client.LHIA.service;

import com.marcicomentariosfacebook.client.LHIA.models.AuthResponse;
import com.marcicomentariosfacebook.client.LHIA.models.RequestQuestionLhia;
import com.marcicomentariosfacebook.client.LHIA.models.ResponseLhia;
import reactor.core.publisher.Mono;

public interface ApiLhiaService {

	Mono<ResponseLhia> sendMesssageToLhia(RequestQuestionLhia data, String token);

	Mono<AuthResponse> getToken();

}
