/*
 * Copyright (C) 2018 Dominik Schadow, dominikschadow@gmail.com
 *
 * This file is part of the Java Security project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.dominikschadow.javasecurity.csrf.order;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;

/**
 * Order controller to process the order and return the result.
 *
 * @author Dominik Schadow
 */
@Controller
public class OrderController extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @PostMapping
    public String storeOrder(HttpServletRequest request, Order order) throws ServletException {
        log.info("Processing order...");
        String message;

        if (!CSRFTokenHandler.isValid(request)) {
            message = "Anti CSRF token is invalid.";
        } else {
            message = "Anti CSRF token is valid. Ordered " + order.getQuantity() + " of product " + order.getProduct();
        }

        log.info(message);

        return "result";
    }
}
