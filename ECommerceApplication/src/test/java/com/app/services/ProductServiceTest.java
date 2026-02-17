package com.app.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.mock.web.MockMultipartFile;

import com.app.entites.Category;
import com.app.entites.Product;
import com.app.exceptions.APIException;
import com.app.exceptions.ResourceNotFoundException;
import com.app.payloads.ProductDTO;
import com.app.payloads.ProductResponse;
import com.app.repositories.CartRepo;
import com.app.repositories.CategoryRepo;
import com.app.repositories.ProductRepo;

/**
 * Test untuk ProductService - fitur katalog produk.
 *
 * Sesuai deskripsi:
 * - Katalog standar
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Tests - Product Catalog")
class ProductServiceTest {

    @Mock
    private ProductRepo productRepo;

    @Mock
    private CategoryRepo categoryRepo;

    @Mock
    private CartRepo cartRepo;

    @Mock
    private CartService cartService;

    @Mock
    private ModelMapper modelMapper;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;
    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setCategoryId(1L);
        category.setCategoryName("Electronics");

        product = new Product();
        product.setProductId(1L);
        product.setProductName("Laptop");
        product.setDescription("High performance laptop");
        product.setPrice(15000000.0);
        product.setDiscount(10.0);
        product.setSpecialPrice(13500000.0);
        product.setQuantity(50);
        product.setImage("laptop.jpg");
        product.setCategory(category);

        productDTO = new ProductDTO();
        productDTO.setProductId(1L);
        productDTO.setProductName("Laptop");
        productDTO.setDescription("High performance laptop");
        productDTO.setPrice(15000000.0);
        productDTO.setDiscount(10.0);
        productDTO.setSpecialPrice(13500000.0);
        productDTO.setQuantity(50);
        productDTO.setImage("laptop.jpg");
    }

    @Nested
    @DisplayName("Add Product Tests")
    class AddProductTests {

        @Test
        @DisplayName("Berhasil menambahkan produk ke kategori")
        void addProduct_Success() {
            // Category dengan products kosong
            category.setProducts(new java.util.ArrayList<>());
            
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(productRepo.save(any(Product.class))).thenReturn(product);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            ProductDTO result = productService.addProduct(1L, product);

            assertNotNull(result);
            assertEquals("Laptop", result.getProductName());
            verify(productRepo).save(any(Product.class));
        }

        @Test
        @DisplayName("Gagal menambahkan produk ke kategori yang tidak ada")
        void addProduct_CategoryNotFound_ThrowsException() {
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> productService.addProduct(999L, product));

            verify(productRepo, never()).save(any());
        }

        @Test
        @DisplayName("Gagal menambahkan produk dengan nama dan deskripsi yang sudah ada di kategori")
        void addProduct_DuplicateInCategory_ThrowsException() {
            // Existing product dengan nama dan deskripsi sama
            Product existingProduct = new Product();
            existingProduct.setProductId(2L);
            existingProduct.setProductName("Laptop");
            existingProduct.setDescription("High performance laptop");
            
            category.setProducts(Arrays.asList(existingProduct));

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));

            APIException exception = assertThrows(APIException.class,
                () -> productService.addProduct(1L, product));

            assertTrue(exception.getMessage().contains("exists"));
            verify(productRepo, never()).save(any());
        }

        @Test
        @DisplayName("Special price dihitung dengan benar dari diskon")
        void addProduct_CalculatesSpecialPrice() {
            category.setProducts(new java.util.ArrayList<>());
            product.setPrice(100000.0);
            product.setDiscount(20.0);
            // Expected special price: 100000 - (100000 * 20/100) = 80000

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(productRepo.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                assertEquals(80000.0, p.getSpecialPrice());
                return p;
            });
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(productDTO);

            productService.addProduct(1L, product);

            verify(productRepo).save(any());
        }
    }

    @Nested
    @DisplayName("Get All Products Tests")
    class GetAllProductsTests {

        @Test
        @DisplayName("Berhasil mendapatkan semua produk dengan pagination")
        void getAllProducts_WithPagination_Success() {
            List<Product> products = Arrays.asList(product);
            Page<Product> productPage = new PageImpl<>(products);

            when(productRepo.findAll(any(Pageable.class))).thenReturn(productPage);
            when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

            ProductResponse result = productService.getAllProducts(0, 10, "productId", "asc");

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Laptop", result.getContent().get(0).getProductName());
        }

        @Test
        @DisplayName("Response memiliki informasi pagination")
        void getAllProducts_HasPaginationInfo() {
            List<Product> products = Arrays.asList(product);
            Page<Product> productPage = new PageImpl<>(products);

            when(productRepo.findAll(any(Pageable.class))).thenReturn(productPage);
            when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

            ProductResponse result = productService.getAllProducts(0, 10, "productId", "asc");

            assertNotNull(result.getPageNumber());
            assertNotNull(result.getPageSize());
            assertNotNull(result.getTotalPages());
            assertNotNull(result.getTotalElements());
        }

        @Test
        @DisplayName("Return empty list saat tidak ada produk")
        void getAllProducts_Empty_ReturnsEmptyList() {
            Page<Product> emptyPage = new PageImpl<>(Arrays.asList());

            when(productRepo.findAll(any(Pageable.class))).thenReturn(emptyPage);

            ProductResponse result = productService.getAllProducts(0, 10, "productId", "asc");

            assertNotNull(result);
            assertTrue(result.getContent().isEmpty());
        }
    }

    @Nested
    @DisplayName("Search By Category Tests")
    class SearchByCategoryTests {

        @Test
        @DisplayName("Berhasil mencari produk berdasarkan kategori")
        void searchByCategory_Success() {
            List<Product> products = Arrays.asList(product);
            Page<Product> productPage = new PageImpl<>(products);

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(productRepo.findByCategory(eq(category), any(Pageable.class))).thenReturn(productPage);
            when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

            ProductResponse result = productService.searchByCategory(1L, 0, 10, "productId", "asc");

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
        }

        @Test
        @DisplayName("Gagal mencari dengan kategori tidak ditemukan")
        void searchByCategory_CategoryNotFound_ThrowsException() {
            when(categoryRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> productService.searchByCategory(999L, 0, 10, "productId", "asc"));
        }

        @Test
        @DisplayName("Exception saat tidak ada produk di kategori")
        void searchByCategory_NoProducts_ThrowsException() {
            Page<Product> emptyPage = new PageImpl<>(Arrays.asList());

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(category));
            when(productRepo.findByCategory(eq(category), any(Pageable.class))).thenReturn(emptyPage);

            APIException exception = assertThrows(APIException.class,
                () -> productService.searchByCategory(1L, 0, 10, "productId", "asc"));

            assertTrue(exception.getMessage().contains(category.getCategoryName()) ||
                       exception.getMessage().toLowerCase().contains("no product"));
        }
    }

    @Nested
    @DisplayName("Search By Keyword Tests")
    class SearchByKeywordTests {

        @Test
        @DisplayName("Berhasil mencari produk dengan keyword")
        void searchProductByKeyword_Success() {
            List<Product> products = Arrays.asList(product);
            Page<Product> productPage = new PageImpl<>(products);

            when(productRepo.findByProductNameLike(eq("%Laptop%"), any(Pageable.class)))
                .thenReturn(productPage);
            when(modelMapper.map(product, ProductDTO.class)).thenReturn(productDTO);

            ProductResponse result = productService.searchProductByKeyword("Laptop", 0, 10, "productId", "asc");

            assertNotNull(result);
            assertEquals(1, result.getContent().size());
            assertEquals("Laptop", result.getContent().get(0).getProductName());
        }

        @Test
        @DisplayName("Exception saat keyword tidak menemukan produk")
        void searchProductByKeyword_NotFound_ThrowsException() {
            Page<Product> emptyPage = new PageImpl<>(Arrays.asList());

            when(productRepo.findByProductNameLike(eq("%Unknown%"), any(Pageable.class)))
                .thenReturn(emptyPage);

            APIException exception = assertThrows(APIException.class,
                () -> productService.searchProductByKeyword("Unknown", 0, 10, "productId", "asc"));

            assertTrue(exception.getMessage().contains("Unknown") ||
                       exception.getMessage().toLowerCase().contains("not found"));
        }
    }

    @Nested
    @DisplayName("Update Product Tests")
    class UpdateProductTests {

        @Test
        @DisplayName("Berhasil mengupdate produk")
        void updateProduct_Success() {
            Product updatedProduct = new Product();
            updatedProduct.setProductId(1L);
            updatedProduct.setProductName("Updated Laptop");
            updatedProduct.setPrice(16000000.0);
            updatedProduct.setDiscount(10.0);

            ProductDTO updatedDTO = new ProductDTO();
            updatedDTO.setProductId(1L);
            updatedDTO.setProductName("Updated Laptop");

            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(cartRepo.findCartsByProductId(1L)).thenReturn(new java.util.ArrayList<>());
            when(productRepo.save(any(Product.class))).thenReturn(updatedProduct);
            when(modelMapper.map(any(Product.class), eq(ProductDTO.class))).thenReturn(updatedDTO);

            ProductDTO result = productService.updateProduct(1L, updatedProduct);

            assertNotNull(result);
            assertEquals("Updated Laptop", result.getProductName());
        }

        @Test
        @DisplayName("Gagal update produk yang tidak ditemukan")
        void updateProduct_NotFound_ThrowsException() {
            when(productRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> productService.updateProduct(999L, product));
        }
    }

    @Nested
    @DisplayName("Delete Product Tests")
    class DeleteProductTests {

        @Test
        @DisplayName("Berhasil menghapus produk")
        void deleteProduct_Success() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(product));
            when(cartRepo.findCartsByProductId(1L)).thenReturn(new java.util.ArrayList<>());
            doNothing().when(productRepo).delete(product);

            String result = productService.deleteProduct(1L);

            assertNotNull(result);
            assertTrue(result.contains("deleted"));
            verify(productRepo).delete(product);
        }

        @Test
        @DisplayName("Gagal menghapus produk yang tidak ditemukan")
        void deleteProduct_NotFound_ThrowsException() {
            when(productRepo.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                () -> productService.deleteProduct(999L));

            verify(productRepo, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Product Price Tests")
    class ProductPriceTests {

        @Test
        @DisplayName("Produk memiliki harga valid")
        void product_HasValidPrice() {
            assertTrue(product.getPrice() > 0);
        }

        @Test
        @DisplayName("Special price lebih kecil atau sama dengan price")
        void product_SpecialPriceLessThanOrEqualPrice() {
            assertTrue(product.getSpecialPrice() <= product.getPrice());
        }

        @Test
        @DisplayName("Discount dalam range valid (0-100)")
        void product_DiscountInValidRange() {
            assertTrue(product.getDiscount() >= 0);
            assertTrue(product.getDiscount() <= 100);
        }
    }

    @Nested
    @DisplayName("Product Quantity Tests")
    class ProductQuantityTests {

        @Test
        @DisplayName("Produk memiliki quantity non-negative")
        void product_HasNonNegativeQuantity() {
            assertTrue(product.getQuantity() >= 0);
        }
    }
}
