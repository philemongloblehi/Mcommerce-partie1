package com.ecommerce.microcommerce.web.controller;

import com.ecommerce.microcommerce.dao.ProductDao;
import com.ecommerce.microcommerce.model.Product;
import com.ecommerce.microcommerce.service.ProductService;
import com.ecommerce.microcommerce.web.exceptions.ProduitGratuitException;
import com.ecommerce.microcommerce.web.exceptions.ProduitIntrouvableException;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.json.MappingJacksonValue;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import javax.validation.Valid;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestController
@Api(description = "API pour les oprérations CRUD sur les produits" )
public class ProductController {

    @Autowired
    private ProductDao productDao;

    @Autowired
    private ProductService productService;

    //Recuperer la liste des produits
    @GetMapping(value = "/Produits")
    public MappingJacksonValue listeProduits() {
        Iterable<Product> produits = productDao.findAll();

        SimpleBeanPropertyFilter monFiltre = SimpleBeanPropertyFilter.serializeAllExcept("prixAchat");

        FilterProvider listDeNosFiltres = new SimpleFilterProvider().addFilter("monFiltreDynamique", monFiltre);

        MappingJacksonValue produitsFiltres = new MappingJacksonValue(produits);

        produitsFiltres.setFilters(listDeNosFiltres);

        return produitsFiltres;
    }

    //Recuperer un produit par son id
    @ApiOperation(value = "Récupère un produit grâce à son ID à condition que celui-ci soit en stock!")
    @GetMapping(value = "/Produits/{id}")
    public Product afficherUnProduit(@PathVariable int id) {
        Product produit = productDao.findById(id);
        if (produit == null) {
            throw new ProduitIntrouvableException("Le produit avec l'id " + id + " est INTROUVABLE, ecran bleu si je pouvais");
        }
        return produit;
    }

    @GetMapping(value = "test/produits/{prixLimit}")
    public List<Product> testDeRequetes(@PathVariable int prixLimit) {
        return productDao.findByPrixGreaterThan(prixLimit);
    }

    @GetMapping(value = "test/produits/{recherche}")
    public List<Product> testDeRequetes(@PathVariable String recherche) {
        return productDao.findByNomLike("%" + recherche + "%");
    }

    //Ajouter un produit
    @PostMapping(value = "/Produits")
    public ResponseEntity<Void> ajouterProduit(@Valid @RequestBody Product product) {
        if (product.getPrix() <= 0) {
            throw new ProduitGratuitException("Le produit ne peut pas avoir un prix inferieur ou egale a 0");
        }
        Product productAdded = productDao.save(product);

        if (productAdded == null) {
            return ResponseEntity.noContent().build();
        }
        URI location = ServletUriComponentsBuilder
                .fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(productAdded.getId())
                .toUri();
        return ResponseEntity.created(location).build();
    }

    @DeleteMapping(value = "/Produits/{id}")
    public void supprimerProduit(@PathVariable int id) {
        Product product = productDao.findById(id);
        if (product != null) {
            productDao.delete(product);
        }
    }

    @PutMapping(value = "/Produits")
    public void modifierProduit(@RequestBody Product product) {
        productDao.save(product);
    }

    @GetMapping(value = "/AdminProduits")
    public List<String> calculerMargeProduit() {
        List<Product> products = productDao.findAll();
        if (products != null) {
            List<String> productList = new ArrayList<>();
            int marge;
            for (Product product : products) {
                marge = productService.calculMarge(product.getPrix(), product.getPrixAchat());
                productList.add(product.toString() + ": " + marge);
            }
            return productList;
        }
        return null;
    }

    @GetMapping(value = "/Produits/filter")
    public List<Product> trierProduitsParOrdreAlphabetique() {
        return productDao.findAllByOrderByNomAsc();
    }
}
