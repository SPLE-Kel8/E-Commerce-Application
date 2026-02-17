# Clone-and-Own Software Product Line (SPLE) Guide

## Overview

This E-Commerce application is designed for **Clone-and-Own** reuse in a Software Product Line context. This guide explains how to customize the application for different product variants with minimal code changes.

## Architecture for Clone-and-Own

```
src/main/java/com/app/
├── config/
│   ├── AppConstants.java      # Centralized constants (modify for variants)
│   ├── FeatureConfig.java     # Feature flags (reads from application.properties)
│   └── ...
├── controllers/               # REST endpoints (use FeatureConfig for feature checks)
├── entites/                   # JPA entities
├── services/                  # Business logic (use AppConstants)
├── repositories/              # Data access (Spring Data JPA)
├── payloads/                  # DTOs for API requests/responses
└── exceptions/                # Custom exceptions
```

## Feature Configuration

### 1. Payment Methods

**Current Configuration:** Bank Transfer only

To modify payment methods, edit `application.properties`:

```properties
# Enable/disable payment methods
app.payment.bank-transfer.enabled=true
app.payment.credit-card.enabled=false
app.payment.e-wallet.enabled=false
```

To add a new payment method:
1. Add feature flag in `application.properties`
2. Add constant in `AppConstants.java` (e.g., `PAYMENT_CREDIT_CARD`)
3. Update `FeatureConfig.isPaymentMethodEnabled()` method

### 2. Promo Code Feature

**Current Configuration:** Enabled

To disable promo codes:

```properties
app.feature.promo-code.enabled=false
```

When disabled:
- All promo code endpoints return 400 Bad Request
- Cart promo code endpoints are blocked
- No code changes required

### 3. Product Discount Feature

```properties
app.feature.product-discount.enabled=true
```

## Key Files to Modify for Clone-and-Own

### AppConstants.java
Central location for all constants. Modify these sections:

| Section | Purpose |
|---------|---------|
| PAGINATION | Default page size, numbers |
| SORTING | Default sort fields |
| USER ROLES | Role IDs |
| PAYMENT CONFIGURATION | Supported payment methods |
| PROMO CODE CONFIGURATION | Promo code constraints |
| ORDER STATUS | Order status strings |

### application.properties
Feature flags and configuration:

| Property | Description |
|----------|-------------|
| `app.payment.*.enabled` | Enable/disable payment methods |
| `app.feature.promo-code.enabled` | Enable/disable promo codes |
| `app.feature.product-discount.enabled` | Enable/disable product discounts |
| `app.payment.bank-transfer.*` | Bank transfer account details |

### FeatureConfig.java
Runtime feature checks. Add new feature flags here when needed.

## Creating a New Product Variant

### Step 1: Clone the Repository
```bash
git clone <repository-url> new-webshop
cd new-webshop
```

### Step 2: Configure Features
Edit `src/main/resources/application.properties`:

```properties
# Example: E-Wallet + Bank Transfer variant
app.payment.bank-transfer.enabled=true
app.payment.e-wallet.enabled=true
app.payment.credit-card.enabled=false
app.feature.promo-code.enabled=true
```

### Step 3: Update Constants (if needed)
Edit `AppConstants.java` for variant-specific values.

### Step 4: Database Configuration
Update database credentials in `application.properties` or `.env`:

```properties
DB_HOST=localhost
DB_PORT=5432
DB_NAME=new_webshop_db
DB_USER=user
DB_PASSWORD=password
```

## Feature Matrix

| Feature | This Variant | Notes |
|---------|--------------|-------|
| Catalog | ✅ | Standard product catalog |
| Cart | ✅ | Shopping cart with quantity management |
| Checkout | ✅ | Order placement |
| Bank Transfer Payment | ✅ | BANK_TRANSFER method |
| Credit Card Payment | ❌ | Can enable via config |
| E-Wallet Payment | ❌ | Can enable via config |
| Promo Code Discounts | ✅ | Percentage-based discounts |
| Product Discounts | ✅ | Built-in product special price |

## Extending the Application

### Adding a New Payment Method

1. **application.properties:**
   ```properties
   app.payment.new-method.enabled=true
   ```

2. **FeatureConfig.java:**
   ```java
   @Value("${app.payment.new-method.enabled:false}")
   private boolean newMethodEnabled;
   
   // Update isPaymentMethodEnabled() switch
   ```

3. **AppConstants.java:**
   ```java
   public static final String PAYMENT_NEW_METHOD = "NEW_METHOD";
   ```

### Adding a New Feature

1. Add feature flag to `application.properties`
2. Add `@Value` property to `FeatureConfig.java`
3. Create feature check method if needed
4. Use in controllers: `if (!featureConfig.isFeatureEnabled()) { throw ... }`

## Testing Feature Variants

Run with different profiles:
```bash
# Default (bank transfer, promo codes)
mvn spring-boot:run

# With custom properties
mvn spring-boot:run -Dspring-boot.run.arguments="--app.payment.credit-card.enabled=true"
```

## Maintenance Guidelines

1. **Never hardcode** - Use `AppConstants` or `application.properties`
2. **Feature checks in controllers** - Not in services (keeps business logic reusable)
3. **Document changes** - Update this file when adding features
4. **Use interfaces** - Services use interfaces for flexibility
5. **Lombok for entities** - Reduces boilerplate, easier maintenance

## File Dependencies

```
PromoCode Feature:
├── entities/PromoCode.java
├── repositories/PromoCodeRepo.java
├── payloads/PromoCodeDTO.java
├── services/PromoCodeService.java
├── services/PromoCodeServiceImpl.java
├── controllers/PromoCodeController.java
└── config/FeatureConfig.java (feature flag)

Cart Promo Integration:
├── entities/Cart.java (appliedPromoCode, discountAmount, finalPrice)
├── payloads/CartDTO.java
├── services/CartService.java (applyPromoCode, removePromoCode)
├── services/CartServiceImpl.java
└── controllers/CartController.java (promo endpoints)
```
