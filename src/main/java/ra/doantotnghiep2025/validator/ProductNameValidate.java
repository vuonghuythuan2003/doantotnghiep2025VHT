package ra.doantotnghiep2025.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ra.doantotnghiep2025.repository.ProductRepository;

@Component
public class ProductNameValidate implements ConstraintValidator<ProductNameUnique, String> {
    @Autowired
    private  ProductRepository productRepository;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.trim().isEmpty()) {
            return false;
        }
        return !productRepository.existsByProductName(value);
    }
}
