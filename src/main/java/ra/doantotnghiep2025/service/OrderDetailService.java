package ra.doantotnghiep2025.service;

import ra.doantotnghiep2025.model.dto.BestSellerProductDTO;
import ra.doantotnghiep2025.model.dto.RevenueByCategoryDTO;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderDetailService {
    List<RevenueByCategoryDTO> getRevenueByCategory();

    List<BestSellerProductDTO> getBestSellerProducts(LocalDateTime from, LocalDateTime to);
}
