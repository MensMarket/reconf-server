/*
 *    Copyright 2013-2014 ReConf Team
 *
 *   Licensed under the Apache License, Version 2.0 (the "License");
 *   you may not use this file except in compliance with the License.
 *   You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *   Unless required by applicable law or agreed to in writing, software
 *   distributed under the License is distributed on an "AS IS" BASIS,
 *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *   See the License for the specific language governing permissions and
 *   limitations under the License.
 */
package reconf.server.services.component;

import java.util.*;
import javax.servlet.http.*;
import org.springframework.beans.factory.annotation.*;
import org.springframework.http.*;
import org.springframework.security.core.*;
import org.springframework.transaction.annotation.*;
import org.springframework.web.bind.annotation.*;
import reconf.server.domain.*;
import reconf.server.domain.result.*;
import reconf.server.repository.*;
import reconf.server.services.*;

@CrudService
public class UpsertComponentService {

    @Autowired ProductRepository products;
    @Autowired ComponentRepository components;

    @RequestMapping(value="/product/{prod}/component/{comp}", method=RequestMethod.PUT)
    @Transactional
    public ResponseEntity<ComponentResult> doIt(
            @PathVariable("prod") String productId,
            @PathVariable("comp") String componentId,
            @RequestParam(value="desc", required=false) String description,
            HttpServletRequest request,
            Authentication auth) {

        ComponentKey key = new ComponentKey(productId, componentId);
        Component reqComponent = new Component(key, description);

        List<String> errors = DomainValidator.checkForErrors(key);
        if (!errors.isEmpty()) {
            return new ResponseEntity<ComponentResult>(new ComponentResult(reqComponent, errors), HttpStatus.BAD_REQUEST);
        }

        Product product = products.findOne(key.getProduct());
        if (product == null) {
            return new ResponseEntity<ComponentResult>(new ComponentResult(reqComponent, Product.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
        HttpStatus status = null;
        Component dbComponent = components.findOne(key);
        if (dbComponent == null) {
            dbComponent = new Component(key, description);
            components.save(dbComponent);
            status = HttpStatus.CREATED;

        } else {
            dbComponent.setDescription(description);
            status = HttpStatus.OK;
        }
        return new ResponseEntity<ComponentResult>(new ComponentResult(dbComponent, CrudServiceUtils.getBaseUrl(request)), status);
    }
}
