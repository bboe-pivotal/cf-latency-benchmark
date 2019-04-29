package io.pivotal.bboe.httpserver;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestWebController {
    private static Log logger = LogFactory.getLog(TestWebController.class);

    @RequestMapping("/{input}")
    public String ping(@PathVariable("input") String input) {
        logger.debug("ping(" + input+") -> " + input);
        return input;
    }
}
