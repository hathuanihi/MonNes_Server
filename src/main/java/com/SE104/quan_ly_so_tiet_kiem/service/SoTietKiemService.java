package com.SE104.quan_ly_so_tiet_kiem.service;

import com.SE104.quan_ly_so_tiet_kiem.dto.LoaiSoTietKiemDTO; 
import com.SE104.quan_ly_so_tiet_kiem.dto.SoTietKiemDTO;
import com.SE104.quan_ly_so_tiet_kiem.dto.SoTietKiemRequest;
import com.SE104.quan_ly_so_tiet_kiem.entity.LoaiSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import com.SE104.quan_ly_so_tiet_kiem.entity.SoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.ThayDoi;
import com.SE104.quan_ly_so_tiet_kiem.repository.LoaiSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.MoSoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.NguoiDungRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.SoTietKiemRepository;
import com.SE104.quan_ly_so_tiet_kiem.repository.ThayDoiRepository;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;
import java.time.LocalDate;

@Service
public class SoTietKiemService {

    @Autowired
    private SoTietKiemRepository soTietKiemRepository;
    @Autowired
    private ThayDoiRepository thayDoiRepository;
    @Autowired
    private NguoiDungRepository nguoiDungRepository;
    @Autowired
    private LoaiSoTietKiemRepository loaiSoTietKiemDanhMucRepository;
    @Autowired 
    private MoSoTietKiemRepository moSoTietKiemRepository;


    @Transactional(readOnly = true)
    public List<SoTietKiemDTO> getAllSoTietKiemDTOs() {
        return soTietKiemRepository.findAll().stream()
                .map(this::mapEntityToDto)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public SoTietKiemDTO getSoTietKiemDTOById(Integer id) {
        SoTietKiem soTietKiem = soTietKiemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm sổ tiết kiệm không tồn tại với ID: " + id));
        return mapEntityToDto(soTietKiem);
    }
    
    private LoaiSoTietKiemDTO mapLoaiSoTietKiemDanhMucToDto(LoaiSoTietKiem danhMucEntity) {
        if (danhMucEntity == null) return null;
        LoaiSoTietKiemDTO dto = new LoaiSoTietKiemDTO();
        dto.setMaLoaiDanhMuc(danhMucEntity.getMaLoaiDanhMuc());
        dto.setTenLoaiDanhMuc(danhMucEntity.getTenLoaiDanhMuc());
        return dto;
    }
    
    private SoTietKiemDTO mapEntityToDto(SoTietKiem entity) {
        if (entity == null) return null;
        SoTietKiemDTO dto = new SoTietKiemDTO();
        BeanUtils.copyProperties(entity, dto); 
        dto.setMaSo(entity.getMaSo()); 
        if (entity.getLoaiSoTietKiemDanhMuc() != null) {
            dto.setLoaiSoTietKiemDanhMuc(mapLoaiSoTietKiemDanhMucToDto(entity.getLoaiSoTietKiemDanhMuc()));
        }
        return dto;
    }

    @Transactional
    public SoTietKiemDTO createSoTietKiem(SoTietKiemRequest req, Integer adminId) {
        NguoiDung admin = nguoiDungRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin không tồn tại với ID: " + adminId));
        if (admin.getVaiTro() != 0) {
            throw new SecurityException("Người dùng không có quyền thực hiện hành động này.");
        }

        if (soTietKiemRepository.existsByTenSo(req.getTenSo())) {
            throw new IllegalArgumentException("Tên sản phẩm sổ tiết kiệm '" + req.getTenSo() + "' đã tồn tại.");
        }

        LoaiSoTietKiem danhMuc = loaiSoTietKiemDanhMucRepository.findById(req.getMaLoaiDanhMuc())
                .orElseThrow(() -> new EntityNotFoundException("Danh mục loại sổ không tồn tại với ID: " + req.getMaLoaiDanhMuc()));

        SoTietKiem soTietKiem = new SoTietKiem();
        mapRequestToEntity(req, soTietKiem, danhMuc);
        
        validateSoTietKiemProduct(soTietKiem);

        SoTietKiem saved = soTietKiemRepository.save(soTietKiem);
        logThayDoi(null, saved, admin, "Tạo mới sản phẩm sổ tiết kiệm: " + saved.getTenSo());
        return mapEntityToDto(saved);
    }

    @Transactional
    public SoTietKiemDTO updateSoTietKiem(Integer id, SoTietKiemRequest req, Integer adminId) {
        NguoiDung admin = nguoiDungRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin không tồn tại với ID: " + adminId));
        if (admin.getVaiTro() != 0) {
            throw new SecurityException("Người dùng không có quyền thực hiện hành động này.");
        }

        SoTietKiem existing = soTietKiemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm sổ tiết kiệm không tồn tại với ID: " + id));
        
        if (!existing.getTenSo().equals(req.getTenSo()) && soTietKiemRepository.existsByTenSo(req.getTenSo())) {
             throw new IllegalArgumentException("Tên sản phẩm sổ tiết kiệm '" + req.getTenSo() + "' đã tồn tại cho một sản phẩm khác.");
        }

        SoTietKiem oldState = new SoTietKiem();
        BeanUtils.copyProperties(existing, oldState); 
        if (existing.getLoaiSoTietKiemDanhMuc() != null) {
            LoaiSoTietKiem oldDanhMuc = new LoaiSoTietKiem();
            BeanUtils.copyProperties(existing.getLoaiSoTietKiemDanhMuc(), oldDanhMuc);
            oldState.setLoaiSoTietKiemDanhMuc(oldDanhMuc);
        }


        LoaiSoTietKiem danhMuc = loaiSoTietKiemDanhMucRepository.findById(req.getMaLoaiDanhMuc())
                .orElseThrow(() -> new EntityNotFoundException("Danh mục loại sổ không tồn tại với ID: " + req.getMaLoaiDanhMuc()));
        
        mapRequestToEntity(req, existing, danhMuc);
        validateSoTietKiemProduct(existing);

        SoTietKiem updated = soTietKiemRepository.save(existing);
        logThayDoi(oldState, updated, admin, "Cập nhật sản phẩm sổ tiết kiệm ID: " + updated.getMaSo());
        return mapEntityToDto(updated);
    }

    private void mapRequestToEntity(SoTietKiemRequest req, SoTietKiem entity, LoaiSoTietKiem danhMuc) {
        entity.setTenSo(req.getTenSo());
        entity.setKyHan(req.getKyHan());
        entity.setLaiSuat(req.getLaiSuat());
        entity.setTienGuiBanDauToiThieu(req.getTienGuiBanDauToiThieu());
        entity.setTienGuiThemToiThieu(req.getTienGuiThemToiThieu());
        entity.setSoNgayGuiToiThieuDeRut(req.getSoNgayGuiToiThieuDeRut());
        entity.setLoaiSoTietKiemDanhMuc(danhMuc);
    }

    @Transactional
    public void deleteSoTietKiem(Integer id, Integer adminId) {
        NguoiDung admin = nguoiDungRepository.findById(adminId)
                .orElseThrow(() -> new EntityNotFoundException("Admin không tồn tại với ID: " + adminId));
        if (admin.getVaiTro() != 0) {
            throw new SecurityException("Người dùng không có quyền thực hiện hành động này.");
        }
        
        SoTietKiem existing = soTietKiemRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Sản phẩm sổ tiết kiệm không tồn tại với ID: " + id));
        
        // Kiểm tra xem sản phẩm có đang được sử dụng bởi MoSoTietKiem nào không
        if (moSoTietKiemRepository.existsBySoTietKiemSanPham_MaSo(id)) {
            throw new IllegalStateException("Không thể xóa sản phẩm sổ tiết kiệm ID " + id + " vì đang có sổ tiết kiệm của người dùng sử dụng sản phẩm này.");
        }
        // Xóa các log thay đổi liên quan đến sản phẩm này trước
        thayDoiRepository.deleteBySoTietKiemSanPham(existing);

        soTietKiemRepository.delete(existing);
        logThayDoi(existing, null, admin, "Xóa sản phẩm sổ tiết kiệm ID: " + id + ", Tên: " + existing.getTenSo());
    }

    private void validateSoTietKiemProduct(SoTietKiem soTietKiem) {
        if (soTietKiem.getTenSo() == null || soTietKiem.getTenSo().trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm sổ không được để trống.");
        }
        if (soTietKiem.getTienGuiBanDauToiThieu() == null || soTietKiem.getTienGuiBanDauToiThieu() < 100000L) {
            throw new IllegalArgumentException("Số tiền gửi ban đầu tối thiểu không hợp lệ (ít nhất 100,000 VND).");
        }
        if (soTietKiem.getTienGuiThemToiThieu() == null || soTietKiem.getTienGuiThemToiThieu() < 100000L) { 
             throw new IllegalArgumentException("Số tiền gửi thêm tối thiểu không hợp lệ (ít nhất 100,000 VND).");
        }
        if (soTietKiem.getKyHan() == null || soTietKiem.getKyHan() < 0) {
            throw new IllegalArgumentException("Kỳ hạn không hợp lệ (tháng, >=0).");
        }
        if (soTietKiem.getLaiSuat() == null || soTietKiem.getLaiSuat().compareTo(new BigDecimal("0.01")) < 0) { 
            throw new IllegalArgumentException("Lãi suất phải lớn hơn 0.");
        }
        if (soTietKiem.getKyHan() == 0 && (soTietKiem.getSoNgayGuiToiThieuDeRut() == null || soTietKiem.getSoNgayGuiToiThieuDeRut() <= 0)) {
            throw new IllegalArgumentException("Sản phẩm không kỳ hạn phải có số ngày gửi tối thiểu để rút hợp lệ (>0 ngày).");
        }
        if (soTietKiem.getKyHan() > 0 && soTietKiem.getSoNgayGuiToiThieuDeRut() != null && soTietKiem.getSoNgayGuiToiThieuDeRut() > 0) {
             // soTietKiem.setSoNgayGuiToiThieuDeRut(null);
        }
    }

    private void logThayDoi(SoTietKiem oldState, SoTietKiem newState, NguoiDung admin, String ghiChu) {
        ThayDoi thayDoi = new ThayDoi();
        thayDoi.setNguoiDungAdmin(admin);
        thayDoi.setNgayThayDoi(LocalDate.now());
        thayDoi.setGhiChu(ghiChu);

        SoTietKiem stateToLogSanPham = (newState != null) ? newState : oldState;
        if (stateToLogSanPham == null) return; // Should not happen if either old or new is present
        thayDoi.setSoTietKiemSanPham(stateToLogSanPham);


        if (oldState != null) {
            thayDoi.setTenSoCu(oldState.getTenSo());
            thayDoi.setKyHanCu(oldState.getKyHan());
            thayDoi.setLaiSuatCu(oldState.getLaiSuat());
            thayDoi.setTienGuiBanDauToiThieuCu(oldState.getTienGuiBanDauToiThieu());
            thayDoi.setTienGuiThemToiThieuCu(oldState.getTienGuiThemToiThieu());
            thayDoi.setSoNgayGuiToiThieuDeRutCu(oldState.getSoNgayGuiToiThieuDeRut());
        }

        if (newState != null) {
            thayDoi.setTenSoMoi(newState.getTenSo());
            thayDoi.setKyHanMoi(newState.getKyHan());
            thayDoi.setLaiSuatMoi(newState.getLaiSuat());
            thayDoi.setTienGuiBanDauToiThieuMoi(newState.getTienGuiBanDauToiThieu());
            thayDoi.setTienGuiThemToiThieuMoi(newState.getTienGuiThemToiThieu());
            thayDoi.setSoNgayGuiToiThieuDeRutMoi(newState.getSoNgayGuiToiThieuDeRut());
        }
        
        thayDoiRepository.save(thayDoi);
    }
    
}