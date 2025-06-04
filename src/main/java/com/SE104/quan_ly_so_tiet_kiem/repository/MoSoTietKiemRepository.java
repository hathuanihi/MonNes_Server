package com.SE104.quan_ly_so_tiet_kiem.repository;

import com.SE104.quan_ly_so_tiet_kiem.entity.MoSoTietKiem;
import com.SE104.quan_ly_so_tiet_kiem.entity.NguoiDung;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface MoSoTietKiemRepository extends JpaRepository<MoSoTietKiem, Integer> {

    List<MoSoTietKiem> findByNguoiDung_MaND(Integer maND);

    Optional<MoSoTietKiem> findByMaMoSoAndNguoiDung_MaND(Integer maMoSo, Integer maND);

    List<MoSoTietKiem> findByTrangThai(MoSoTietKiem.TrangThaiMoSo trangThai);

    List<MoSoTietKiem> findByNguoiDung(NguoiDung nguoiDung);
    
    List<MoSoTietKiem> findByNguoiDung_MaNDAndTrangThai(Integer maND, MoSoTietKiem.TrangThaiMoSo trangThai);
    List<MoSoTietKiem> findByNguoiDungAndTrangThai(NguoiDung nguoiDung, MoSoTietKiem.TrangThaiMoSo trangThai);

    Long countByTrangThai(MoSoTietKiem.TrangThaiMoSo trangThai);

    Integer countByNguoiDung_MaNDAndTrangThai(Integer maND, MoSoTietKiem.TrangThaiMoSo trangThai);

    Integer countByNguoiDungAndTrangThai(NguoiDung nguoiDung, MoSoTietKiem.TrangThaiMoSo trangThai);


    @Query("SELECT SUM(m.soDu) FROM MoSoTietKiem m WHERE m.trangThai = :trangThai")
    BigDecimal sumSoDuByTrangThai(@Param("trangThai") MoSoTietKiem.TrangThaiMoSo trangThai);

    boolean existsBySoTietKiemSanPham_MaSo(Integer maSoSanPham);
}