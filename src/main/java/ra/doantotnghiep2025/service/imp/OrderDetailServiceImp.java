package ra.doantotnghiep2025.service.imp;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import ra.doantotnghiep2025.model.dto.BestSellerProductDTO;
import ra.doantotnghiep2025.model.dto.RevenueByCategoryDTO;
import ra.doantotnghiep2025.model.entity.Category;
import ra.doantotnghiep2025.model.entity.OrderDetail;
import ra.doantotnghiep2025.repository.CategoryRepository;
import ra.doantotnghiep2025.repository.OrderDetailRepository;
import ra.doantotnghiep2025.service.OrderDetailService;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailServiceImp implements OrderDetailService {
    @Autowired
    private OrderDetailRepository orderDetailRepository;
    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<RevenueByCategoryDTO> getRevenueByCategory() {
        List<Category> categories = categoryRepository.findAll();

        return categories.stream().map(category -> {
            List<OrderDetail> orderDetails = orderDetailRepository.findByProductCategory(category);
            BigDecimal totalRevenue = orderDetails.stream()
                    .map(od -> od.getUnitPrice().multiply(BigDecimal.valueOf(od.getOrderQuantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            return new RevenueByCategoryDTO(category.getCategoryId(), category.getCategoryName(), totalRevenue);
        }).collect(Collectors.toList());
    }

    @Override
    public List<BestSellerProductDTO> getBestSellerProducts(LocalDateTime from, LocalDateTime to) {
        return orderDetailRepository.findTop10ByOrder_CreatedAtBetweenOrderByOrderQuantityDesc(from, to, PageRequest.of(0, 10))
                .stream()
                .map(orderDetail -> BestSellerProductDTO.builder()
                        .productId(orderDetail.getProduct().getProductId())
                        .productName(orderDetail.getProduct().getProductName())
                        .totalQuantitySold(orderDetail.getOrderQuantity())
                        .totalRevenue(orderDetail.getUnitPrice().multiply(BigDecimal.valueOf(orderDetail.getOrderQuantity())))
                        .build())
                .collect(Collectors.toList());
    }
}
