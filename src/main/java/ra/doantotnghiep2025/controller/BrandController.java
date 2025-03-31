package ra.doantotnghiep2025.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.BrandRequestDTO;
import ra.doantotnghiep2025.model.dto.BrandResponseDTO;
import ra.doantotnghiep2025.service.BrandService;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/brands")
@RequiredArgsConstructor
public class BrandController {
    @Autowired
    private BrandService brandService;

    @PostMapping(consumes = {"multipart/form-data"})
    public ResponseEntity<BrandResponseDTO> createBrand(
            @Valid @ModelAttribute BrandRequestDTO brandRequestDTO) throws CustomerException {
        BrandResponseDTO brand = brandService.createBrand(brandRequestDTO);
        return new ResponseEntity<>(brand, HttpStatus.CREATED);
    }

    @PutMapping("/{brandId}")
    public ResponseEntity<BrandResponseDTO> updateBrand(
            @PathVariable Long brandId,
            @Valid @ModelAttribute BrandRequestDTO brandRequestDTO) throws CustomerException {
        BrandResponseDTO updatedBrand = brandService.updateBrand(brandId, brandRequestDTO);
        return ResponseEntity.ok(updatedBrand);
    }

    @DeleteMapping("/{brandId}")
    public ResponseEntity<Void> deleteBrand(@Valid @PathVariable Long brandId) throws CustomerException {
        brandService.deleteBrand(brandId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<BrandResponseDTO>> searchBrands(@Valid @RequestParam String keyword) {
        List<BrandResponseDTO> brands = brandService.searchBrands(keyword);
        return ResponseEntity.ok(brands);
    }

    @GetMapping
    public ResponseEntity<Page<BrandResponseDTO>> getBrands(
            @Valid
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "brandName") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        return ResponseEntity.ok(brandService.getBrands(page, size, sortBy, direction));
    }

    @GetMapping("/categories/{categoryId}")
    public ResponseEntity<List<BrandResponseDTO>> getBrandsByCategory(
            @Valid
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        List<BrandResponseDTO> brands = brandService.getBrandsByCategory(categoryId, page, size);
        return ResponseEntity.ok(brands);
    }

    @GetMapping("/{brandId}")
    public ResponseEntity<BrandResponseDTO> getBrandById(@PathVariable Long brandId) throws CustomerException {
        BrandResponseDTO brand = brandService.getBrandById(brandId);
        return ResponseEntity.ok(brand);
    }
}