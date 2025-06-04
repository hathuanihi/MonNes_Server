package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.GiaoDichDTO;
import com.SE104.quan_ly_so_tiet_kiem.entity.GiaoDich;
import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem; 
import com.SE104.quan_ly_so_tiet_kiem.model.TransactionType;
import com.SE104.quan_ly_so_tiet_kiem.repository.GiaoDichRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger; 
import org.slf4j.LoggerFactory; 
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GiaoDichService {
    private static final Logger logger = LoggerFactory.getLogger(GiaoDichService.class);

    @Autowired
    private GiaoDichRepository giaoDichRepository;
    @Autowired
    private MoSoTietKiemRepository moSoTietKiemRepository;

    @Transactional
    public GiaoDich saveTransaction(BigDecimal soTien, TransactionType loaiGiaoDich, MoSoTietKiem moSoTietKiem) {
        if (moSoTietKiem == null) {
            logger.error("Không thể tạo giao dịch: Thông tin Sổ Tiết Kiệm Mở (MoSoTietKiem) là null.");
            throw new IllegalArgumentException("Sổ tiết kiệm không được null để ghi giao dịch.");
        }
        if (moSoTietKiem.getSoTietKiemSanPham() == null) {
            logger.error("Không thể tạo giao dịch cho MoSoTietKiem ID {}: Thông tin Sản Phẩm Sổ Tiết Kiệm (SoTietKiemSanPham) là null.", moSoTietKiem.getMaMoSo());
            throw new IllegalStateException("Không thể xác định sản phẩm sổ tiết kiệm cho giao dịch do MoSoTietKiem không có thông tin SoTietKiemSanPham.");
        }

        GiaoDich giaoDich = new GiaoDich();
        giaoDich.setSoTien(soTien);
        giaoDich.setLoaiGiaoDich(loaiGiaoDich);
        giaoDich.setNgayThucHien(LocalDate.now());
        giaoDich.setMoSoTietKiem(moSoTietKiem);
        giaoDich.setSanPhamSoTietKiem(moSoTietKiem.getSoTietKiemSanPham()); 

        logger.info("Đang lưu giao dịch: Loại {}, Số tiền {}, cho MoSoTietKiem ID {}", loaiGiaoDich, soTien, moSoTietKiem.getMaMoSo());
        return giaoDichRepository.save(giaoDich);
    }

    @Transactional(readOnly = true)
    public Page<GiaoDichDTO> getAllSystemTransactions(Pageable pageable) {
        if (pageable.getSort().isUnsorted()) {
            pageable = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize(), Sort.by("ngayThucHien").descending().and(Sort.by("id").descending()));
        }
        Page<GiaoDich> giaoDichPage = giaoDichRepository.findAll(pageable);
        return giaoDichPage.map(this::mapEntityToDTO);
    }

    @Transactional(readOnly = true)
    public List<GiaoDichDTO> getTransactionsForMoSoTietKiem(Integer moSoTietKiemId, Integer userIdAuth) {
        MoSoTietKiem moSo = moSoTietKiemRepository.findById(moSoTietKiemId)
                .orElseThrow(() -> {
                    logger.warn("Không tìm thấy sổ tiết kiệm với ID: {} khi lấy giao dịch.", moSoTietKiemId);
                    return new EntityNotFoundException("Sổ tiết kiệm không tồn tại ID: " + moSoTietKiemId);
                });
        
        if (moSo.getNguoiDung() == null) {
            logger.error("Sổ tiết kiệm ID {} không có thông tin người dùng.", moSoTietKiemId);
            throw new IllegalStateException("Sổ tiết kiệm ID " + moSoTietKiemId + " không có thông tin người dùng hợp lệ.");
        }
        if (!moSo.getNguoiDung().getMaND().equals(userIdAuth)) {
            logger.warn("Người dùng ID {} cố gắng truy cập giao dịch của sổ ID {} không thuộc sở hữu.", userIdAuth, moSoTietKiemId);
            throw new SecurityException("Không có quyền xem giao dịch của sổ tiết kiệm này.");
        }
        
        List<GiaoDich> giaoDichList = giaoDichRepository.findByMoSoTietKiemOrderByNgayThucHienDesc(moSo);
        return giaoDichList.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
    }
    
    @Transactional(readOnly = true)
    public List<GiaoDichDTO> getRecentTransactionsForUser(Integer userId, int limit) {
        Pageable pageable = PageRequest.of(0, limit, Sort.by("ngayThucHien").descending().and(Sort.by("id").descending()));
        List<GiaoDich> giaoDichList = giaoDichRepository.findByMoSoTietKiem_NguoiDung_MaNDOrderByNgayThucHienDesc(userId, pageable);
        return giaoDichList.stream().map(this::mapEntityToDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public GiaoDichDTO getTransactionDetailsByUser(Long transactionId, Integer userId) {
        GiaoDich giaoDich = giaoDichRepository.findByIdAndMoSoTietKiem_NguoiDung_MaND(transactionId, userId)
                .orElseThrow(() -> {
                    logger.warn("Người dùng ID {} không tìm thấy giao dịch ID {} hoặc không có quyền xem.", userId, transactionId);
                    return new EntityNotFoundException("Không tìm thấy giao dịch với ID: " + transactionId + " hoặc bạn không có quyền xem giao dịch này.");
                });
        return mapEntityToDTO(giaoDich);
    }
    
    @Transactional(readOnly = true) 
    public GiaoDichDTO mapEntityToDTO(GiaoDich entity) {
        if (entity == null) return null;
        GiaoDichDTO dto = new GiaoDichDTO();
        dto.setIdGiaoDich(entity.getId());
        dto.setLoaiGiaoDich(entity.getLoaiGiaoDich() == TransactionType.DEPOSIT ? "Gửi tiền" : "Rút tiền");
        dto.setSoTien(entity.getSoTien());
        dto.setNgayGD(entity.getNgayThucHien());

        MoSoTietKiem moSo = entity.getMoSoTietKiem(); 
        if (moSo != null) {
            try {
                dto.setMaSoMoTietKiem(moSo.getMaMoSo());
                dto.setTenSoMoTietKiem(moSo.getTenSoMo());
                
                NguoiDung nguoiDungCuaSo = moSo.getNguoiDung(); 
                if (nguoiDungCuaSo != null) {
                    dto.setMaKhachHang(nguoiDungCuaSo.getMaND());
                    dto.setTenKhachHang(nguoiDungCuaSo.getTenND());
                }
                
                SoTietKiem sanPham = entity.getSanPhamSoTietKiem(); 
                if (sanPham != null) {
                    dto.setTenSanPhamSoTietKiem(sanPham.getTenSo());
                } 
                // else if (moSo.getSoTietKiemSanPham() != null) { 
                //      dto.setTenSanPhamSoTietKiem(moSo.getSoTietKiemSanPham().getTenSo());
                // }
            } catch (org.hibernate.ObjectNotFoundException | jakarta.persistence.EntityNotFoundException e) {
                logger.error("Lỗi khi truy cập đối tượng liên kết trong mapEntityToDTO cho GiaoDich ID {}: {}", entity.getId(), e.getMessage());
                dto.setTenKhachHang("N/A");
                dto.setTenSoMoTietKiem("N/A");
                dto.setTenSanPhamSoTietKiem("N/A");
            }
        }
        return dto;
    }
}