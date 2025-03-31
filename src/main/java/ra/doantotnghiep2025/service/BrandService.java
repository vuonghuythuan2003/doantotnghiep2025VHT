package ra.doantotnghiep2025.service;

import org.springframework.data.domain.Page;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.BrandRequestDTO;
import ra.doantotnghiep2025.model.dto.BrandResponseDTO;

import java.util.List;

public interface BrandService {
    BrandResponseDTO createBrand(BrandRequestDTO brandRequestDTO) throws CustomerException;

    BrandResponseDTO updateBrand(Long brandId, BrandRequestDTO brandRequestDTO) throws CustomerException;

    void deleteBrand(Long brandId) throws CustomerException;

    List<BrandResponseDTO> searchBrands(String keyword);

    Page<BrandResponseDTO> getBrands(int page, int size, String sortBy, String direction);

    List<BrandResponseDTO> getBrandsByCategory(Long categoryId, int page, int size);

    BrandResponseDTO getBrandById(Long brandId) throws CustomerException;
}