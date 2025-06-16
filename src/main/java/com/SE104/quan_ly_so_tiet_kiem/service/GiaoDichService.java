package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.GiaoDichDTO;
import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.GiaoDichRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification; 
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.criteria.Predicate; 

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GiaoDichService {
    private static final Logger logger = LoggerFactory.getLogger(GiaoDichService.class);
    
    private final GiaoDichRepository giaoDichRepository;
    // private final NguoiDungRepository nguoiDungRepository; 
    private final MoSoTietKiemRepository moSoTietKiemRepository;
    private final Clock clock; 

    @Autowired
    public GiaoDichService(GiaoDichRepository giaoDichRepository, 
                            MoSoTietKiemRepository moSoTietKiemRepository, 
                           Clock clock) {
        this.giaoDichRepository = giaoDichRepository;
        this.moSoTietKiemRepository = moSoTietKiemRepository;
        // this.nguoiDungRepository = nguoiDungRepository;
        this.clock = clock;
    }

    // Phương thức cho USER - Lấy giao dịch gần đây
    @Transactional(readOnly = true)
    public List<GiaoDichDTO> getRecentTransactionsForUser(Integer userId, int limit) {
        logger.info("Fetching recent {} transactions for user ID: {}", limit, userId);
        Pageable pageable = PageRequest.of(0, limit, Sort.by("ngayThucHien").descending().and(Sort.by("id").descending()));
        List<GiaoDich> transactions = giaoDichRepository.findRecentTransactionsByNguoiDungMaND(userId, pageable);
        return transactions.stream().map(this::convertToGiaoDichDTO).collect(Collectors.toList());
    }

    // Phương thức cho USER - Lấy chi tiết một giao dịch
    @Transactional(readOnly = true)
    public GiaoDichDTO getTransactionDetailsByUser(Long transactionId, Integer userId) {
        logger.info("Fetching transaction ID: {} for user ID: {}", transactionId, userId);
        GiaoDich transaction = giaoDichRepository.findByIdAndNguoiDungMaND(transactionId, userId)
                .orElseThrow(() -> new EntityNotFoundException("Không tìm thấy giao dịch ID: " + transactionId + " cho người dùng này."));
        return convertToGiaoDichDTO(transaction);
    }

    // Phương thức cho USER - Lấy tất cả giao dịch của một sổ tiết kiệm cụ thể
    @Transactional(readOnly = true)
    public List<GiaoDichDTO> getTransactionsForMoSoTietKiem(Integer moSoId, Integer userId) {
        logger.info("Fetching all transactions for savings account ID: {} of user ID: {}", moSoId, userId);
        // Kiểm tra xem sổ có thuộc user không
        moSoTietKiemRepository.findByMaMoSoAndNguoiDung_MaND(moSoId, userId)
            .orElseThrow(() -> new SecurityException("Không có quyền truy cập sổ tiết kiệm ID: " + moSoId));
            
        List<GiaoDich> transactions = giaoDichRepository.findByMoSoTietKiem_MaMoSoAndNguoiDung_MaND(moSoId, userId);
        return transactions.stream().map(this::convertToGiaoDichDTO).collect(Collectors.toList());
    }

    @Transactional
    public GiaoDich saveTransaction(BigDecimal soTien, 
                                    TransactionType loaiGiaoDich, 
                                    MoSoTietKiem moSoTietKiem, 
                                    LocalDate ngayThucHienInput) {
        if (moSoTietKiem == null) {
            logger.error("MoSoTietKiem cannot be null when saving a transaction.");
            throw new IllegalArgumentException("Sổ tiết kiệm không được để trống khi ghi giao dịch.");
        }
        if (moSoTietKiem.getSoTietKiemSanPham() == null) {
             logger.error("SoTietKiemSanPham (product) cannot be null in MoSoTietKiem ID {} when saving a transaction.", moSoTietKiem.getMaMoSo());
            throw new IllegalStateException("Không thể xác định sản phẩm sổ tiết kiệm cho giao dịch vì sản phẩm của sổ mở là null.");
        }

        GiaoDich giaoDich = new GiaoDich();
        giaoDich.setSoTien(soTien.setScale(2, RoundingMode.HALF_UP)); // Làm tròn số tiền
        giaoDich.setLoaiGiaoDich(loaiGiaoDich);
        giaoDich.setMoSoTietKiem(moSoTietKiem);
        giaoDich.setSanPhamSoTietKiem(moSoTietKiem.getSoTietKiemSanPham());
        giaoDich.setNgayThucHien(ngayThucHienInput != null ? ngayThucHienInput : LocalDate.now(this.clock));
        
        GiaoDich savedGiaoDich = giaoDichRepository.save(giaoDich);
        logger.info("Saved transaction: ID {}, Type {}, Amount {}, Account ID {}, Date {}", 
                    savedGiaoDich.getId(), loaiGiaoDich, soTien, moSoTietKiem.getMaMoSo(), giaoDich.getNgayThucHien());
        return savedGiaoDich;
    }

    @Transactional(readOnly = true)
    public Page<GiaoDichDTO> getAllSystemTransactions(Pageable pageable, 
                                                   String searchTerm, 
                                                   String transactionType, 
                                                   LocalDate dateFrom, 
                                                   LocalDate dateTo) {
        
        // If no filters are applied, use the simple query with JOIN FETCH
        if ((searchTerm == null || searchTerm.trim().isEmpty()) && 
            (transactionType == null || transactionType.trim().isEmpty()) &&
            dateFrom == null && dateTo == null) {
            
            try {
                // Use the JOIN FETCH query to avoid lazy loading issues
                Page<GiaoDich> giaoDichPage = giaoDichRepository.findAllWithDetails(pageable);
                return giaoDichPage.map(this::convertToGiaoDichDTO);
            } catch (Exception e) {
                logger.error("Error in findAllWithDetails, falling back to specification query", e);
            }
        }
        
        // Use Specification để tạo query động dựa trên các filter
        Specification<GiaoDich> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (searchTerm != null && !searchTerm.trim().isEmpty()) {
                String likePattern = "%" + searchTerm.toLowerCase() + "%";
                predicates.add(criteriaBuilder.or(
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("moSoTietKiem").get("nguoiDung").get("tenND")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("moSoTietKiem").get("tenSoMo")), likePattern),
                    criteriaBuilder.like(criteriaBuilder.lower(root.get("sanPhamSoTietKiem").get("tenSo")), likePattern),
                    criteriaBuilder.like(root.get("id").as(String.class), likePattern) // Tìm theo ID giao dịch
                ));
            }

            if (transactionType != null && !transactionType.trim().isEmpty()) {
                try {
                    TransactionType type = TransactionType.valueOf(transactionType.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("loaiGiaoDich"), type));
                } catch (IllegalArgumentException e) {
                    logger.warn("Invalid transaction type filter: {}", transactionType);
                }
            }

            if (dateFrom != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("ngayThucHien"), dateFrom));
            }
            if (dateTo != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("ngayThucHien"), dateTo));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        Page<GiaoDich> giaoDichPage = giaoDichRepository.findAll(spec, pageable);
        return giaoDichPage.map(this::convertToGiaoDichDTO);
    }
    
    private GiaoDichDTO convertToGiaoDichDTO(GiaoDich entity) {
        if (entity == null) return null;
        
        try {
            GiaoDichDTO dto = new GiaoDichDTO();
            dto.setIdGiaoDich(entity.getId());
            
            if (entity.getLoaiGiaoDich() != null) {
                dto.setLoaiGiaoDich(entity.getLoaiGiaoDich().name()); 
            }
            
            dto.setSoTien(entity.getSoTien());
            
            if (entity.getNgayThucHien() != null) {
                dto.setNgayGD(entity.getNgayThucHien()); 
            }
            
            // Safe handling of lazy loaded relationships
            try {
                if (entity.getMoSoTietKiem() != null) {
                    dto.setMaSoMoTietKiem(entity.getMoSoTietKiem().getMaMoSo());
                    dto.setTenSoMoTietKiem(entity.getMoSoTietKiem().getTenSoMo());
                    
                    if (entity.getMoSoTietKiem().getNguoiDung() != null) {
                        dto.setMaKhachHang(entity.getMoSoTietKiem().getNguoiDung().getMaND());
                        dto.setTenKhachHang(entity.getMoSoTietKiem().getNguoiDung().getTenND());
                    }
                }
            } catch (Exception e) {
                logger.warn("Error loading MoSoTietKiem data for transaction {}: {}", entity.getId(), e.getMessage());
            }
            
            try {
                if (entity.getSanPhamSoTietKiem() != null) {
                    dto.setTenSanPhamSoTietKiem(entity.getSanPhamSoTietKiem().getTenSo());
                }
            } catch (Exception e) {
                logger.warn("Error loading SanPhamSoTietKiem data for transaction {}: {}", entity.getId(), e.getMessage());
            }
            
            return dto;
        } catch (Exception e) {
            logger.error("Error converting GiaoDich to DTO for ID {}: {}", entity.getId(), e.getMessage());
            return null;
        }
    }
}