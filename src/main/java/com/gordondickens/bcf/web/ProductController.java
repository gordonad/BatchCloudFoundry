package com.gordondickens.bcf.web;

import com.gordondickens.bcf.entity.Product;
import com.gordondickens.bcf.repository.ProductRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriUtils;
import org.springframework.web.util.WebUtils;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.io.UnsupportedEncodingException;
import java.util.Collection;

@Controller
@RequestMapping("/products")
public class ProductController {
    private static final Logger logger = LoggerFactory.getLogger(ProductController.class);

    @Inject
    ProductRepository repository;

    @RequestMapping(method = RequestMethod.POST)
    public String create(@Valid Product product, BindingResult bindingResult,
                         Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("product", product);
            return "products/create";
        }

        logger.debug("*****  Received product '{}'  *****", product.toString());
        uiModel.asMap().clear();
        product = repository.saveAndFlush(product);
        logger.debug("***** Saved Product '{}'  *****", product.toString());
        return "redirect:/products/"
                + encodeUrlPathSegment(product.getId().toString(),
                httpServletRequest);
    }

    @RequestMapping(params = "form", method = RequestMethod.GET)
    public String createForm(Model uiModel) {
        uiModel.addAttribute("product", new Product());
        return "products/create";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public String show(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("product", repository.findOne(id));
        uiModel.addAttribute("itemId", id);
        return "products/show";
    }

    @RequestMapping(method = RequestMethod.GET)
    public String list(
            @RequestParam(value = "page", required = false) Integer page,
            @RequestParam(value = "size", required = false) Integer size,
            Model uiModel) {
        if (page != null || size != null) {
            int sizeNo = size == null ? 10 : size;
            // TODO: add pagination
            // uiModel.addAttribute("products", Product.findProductEntries(
            // page == null ? 0 : (page.intValue() - 1) * sizeNo, sizeNo));
            float nrOfPages = (float) repository.count() / sizeNo;
            uiModel.addAttribute(
                    "maxPages",
                    (int) ((nrOfPages > (int) nrOfPages || nrOfPages == 0.0) ? nrOfPages + 1
                            : nrOfPages));
        } else {
            uiModel.addAttribute("products", repository.findAll());
        }
        return "products/list";
    }

    @RequestMapping(method = RequestMethod.PUT)
    public String update(@Valid Product product, BindingResult bindingResult,
                         Model uiModel, HttpServletRequest httpServletRequest) {
        if (bindingResult.hasErrors()) {
            uiModel.addAttribute("product", product);
            return "products/update";
        }
        uiModel.asMap().clear();
        product = repository.saveAndFlush(product);
        // product.merge();
        return "redirect:/products/"
                + encodeUrlPathSegment(product.getId().toString(),
                httpServletRequest);
    }

    @RequestMapping(value = "/{id}", params = "form", method = RequestMethod.GET)
    public String updateForm(@PathVariable("id") Long id, Model uiModel) {
        uiModel.addAttribute("product", repository.findOne(id));
        return "products/update";
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public String delete(@PathVariable("id") Long id,
                         @RequestParam(value = "page", required = false) Integer page,
                         @RequestParam(value = "size", required = false) Integer size,
                         Model uiModel) {
        repository.delete(id);

        uiModel.asMap().clear();
        uiModel.addAttribute("page", (page == null) ? "1" : page.toString());
        uiModel.addAttribute("size", (size == null) ? "10" : size.toString());
        return "redirect:/products";
    }

    @ModelAttribute("products")
    public Collection<Product> populateProducts() {
        return repository.findAll();
    }

    String encodeUrlPathSegment(String pathSegment, HttpServletRequest httpServletRequest) {
        logger.debug("encoding URL Segment '{}'", pathSegment);
        logger.debug("HttpServletRequest '{}'", httpServletRequest.toString());

        String enc = httpServletRequest.getCharacterEncoding();
        logger.debug("Server Encoding set as {}", enc);
        if (enc == null) {
            enc = WebUtils.DEFAULT_CHARACTER_ENCODING;
        }
        try {
            pathSegment = UriUtils.encodePathSegment(pathSegment, enc);
        } catch (UnsupportedEncodingException ignored) {
        }
        return pathSegment;
    }
}
