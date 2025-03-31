package ra.doantotnghiep2025.service.imp;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.exception.CustomerException;
import ra.doantotnghiep2025.model.dto.BrandRequestDTO;
import ra.doantotnghiep2025.model.dto.BrandResponseDTO;
import ra.doantotnghiep2025.model.entity.Brand;
import ra.doantotnghiep2025.model.entity.Category;
import ra.doantotnghiep2025.repository.BrandRepository;
import ra.doantotnghiep2025.repository.CategoryRepository;
import ra.doantotnghiep2025.service.BrandService;
import ra.doantotnghiep2025.service.UploadFileService;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BrandServiceImpl implements BrandService {
    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private UploadFileService uploadFileService;

    @Override
    public BrandResponseDTO createBrand(BrandRequestDTO brandRequestDTO) throws CustomerException {
        if (brandRepository.existsByBrandName(brandRequestDTO.getBrandName())) {
            throw new CustomerException("Tên thương hiệu đã tồn tại");
        }

        Brand brand = new Brand();
        brand.setBrandName(brandRequestDTO.getBrandName());
        brand.setDescription(brandRequestDTO.getDescription());

        if (brandRequestDTO.getImage() != null && !brandRequestDTO.getImage().isEmpty()) {
            String imageUrl = uploadFileService.uploadFile(brandRequestDTO.getImage());
            brand.setImage(imageUrl); // Changed from setImageUrl to setImage
        }

        brand.setStatus(brandRequestDTO.getStatus() != null ? brandRequestDTO.getStatus() : true);

        if (brandRequestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(brandRequestDTO.getCategoryId())
                    .orElseThrow(() -> new CustomerException("Danh mục không tồn tại với ID: " + brandRequestDTO.getCategoryId()));
            brand.setCategory(category);
        }

        brand = brandRepository.save(brand);
        return convertToDto(brand);
    }

    @Override
    public BrandResponseDTO updateBrand(Long brandId, BrandRequestDTO brandRequestDTO) throws CustomerException {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CustomerException("Thương hiệu không tồn tại với ID: " + brandId));

        if (!brand.getBrandName().equals(brandRequestDTO.getBrandName()) &&
                brandRepository.existsByBrandName(brandRequestDTO.getBrandName())) {
            throw new CustomerException("Tên thương hiệu đã tồn tại");
        }

        brand.setBrandName(brandRequestDTO.getBrandName());
        brand.setDescription(brandRequestDTO.getDescription());

        if (brandRequestDTO.getImage() != null && !brandRequestDTO.getImage().isEmpty()) {
            String imageUrl = uploadFileService.uploadFile(brandRequestDTO.getImage());
            brand.setImage(imageUrl); // Changed from setImageUrl to setImage
        }

        brand.setStatus(brandRequestDTO.getStatus() != null ? brandRequestDTO.getStatus() : brand.getStatus());

        if (brandRequestDTO.getCategoryId() != null) {
            Category category = categoryRepository.findById(brandRequestDTO.getCategoryId())
                    .orElseThrow(() -> new CustomerException("Danh mục không tồn tại với ID: " + brandRequestDTO.getCategoryId()));
            brand.setCategory(category);
        } else {
            brand.setCategory(null);
        }

        brand = brandRepository.save(brand);
        return convertToDto(brand);
    }

    @Override
    public void deleteBrand(Long brandId) throws CustomerException {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CustomerException("Thương hiệu không tồn tại với ID: " + brandId));
        brandRepository.delete(brand);
    }

    @Override
    public List<BrandResponseDTO> searchBrands(String keyword) {
        List<Brand> brands = brandRepository.findByBrandNameContainingIgnoreCase(keyword);
        return brands.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public Page<BrandResponseDTO> getBrands(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        PageRequest pageable = PageRequest.of(page, size, sort);
        return brandRepository.findAll(pageable).map(this::convertToDto);
    }

    @Override
    public List<BrandResponseDTO> getBrandsByCategory(Long categoryId, int page, int size) {
        List<Brand> brands = brandRepository.findByCategoryCategoryId(categoryId, PageRequest.of(page, size));
        return brands.stream().map(this::convertToDto).collect(Collectors.toList());
    }

    @Override
    public BrandResponseDTO getBrandById(Long brandId) throws CustomerException {
        Brand brand = brandRepository.findById(brandId)
                .orElseThrow(() -> new CustomerException("Thương hiệu không tồn tại với ID: " + brandId));
        return convertToDto(brand);
    }

    private BrandResponseDTO convertToDto(Brand brand) {
        return BrandResponseDTO.builder()
                .brandId(brand.getBrandId())
                .brandName(brand.getBrandName())
                .description(brand.getDescription())
                .image(brand.getImage()) // Changed from imageUrl to image
                .status(brand.getStatus())
                .categoryId(brand.getCategory() != null ? brand.getCategory().getCategoryId() : null)
                .createdAt(java.sql.Timestamp.valueOf(brand.getCreatedAt()))
                .updatedAt(brand.getUpdatedAt() != null ? java.sql.Timestamp.valueOf(brand.getUpdatedAt()) : null)
                .build();
    }
}