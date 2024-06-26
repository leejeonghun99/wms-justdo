package com.justdo.fruitfruit.model.service;

import com.justdo.fruitfruit.model.dao.*;
import com.justdo.fruitfruit.model.dto.CartDTO;
import com.justdo.fruitfruit.model.dto.CompanyDTO;
import com.justdo.fruitfruit.model.dto.OrderDTO;
import com.justdo.fruitfruit.model.dto.UserDTO;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.justdo.fruitfruit.common.MyBatisTemplate.getSqlSession;

public class UserService {

    /***
     * 회원가입을 처리하는 함수
     * @param userDTO 입력한 회원정보
     * @return 추가된 컬럼개수를 반환
     *         없을경우 0 반환
     */
    public int insertUser(UserDTO userDTO) {

        SqlSession sqlSession = getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            int result =userMapper.insertUser(userDTO);
            sqlSession.commit();
            return result;
        } catch(Exception e) {
            sqlSession.rollback();
            throw new RuntimeException(e);
        } finally {
            sqlSession.close();
        }
    }


    /***
     * 구매자 아이디 찾기를 처리하는 함수
     * @param userDTO 입력한 회원의 이름, 핸드폰번호
     * @return 일치하는 회원의 아이디
     */
    public UserDTO findUserId(UserDTO userDTO) {

        SqlSession sqlSession = getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);

        try {
            UserDTO result = userMapper.findUserId(userDTO);
            return result;
        } catch(Exception e) {
            throw new RuntimeException(e);
        } finally {
            sqlSession.close();
        }

    }

    /***
     * 비밀번호 수정을 처리하는 함수
     * @param userDTO 입력한 아이디, 이름, 휴대폰번호가 일치하는 회원
     * @return 비밀번호 변경 성공시 1 반환
     *         실패시 0 반환
     */
    public int findUserPassword(UserDTO userDTO) {

        SqlSession sqlSession = getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            int result =userMapper.findUserPassword(userDTO);
            sqlSession.commit();
            return result;
        } catch(Exception e) {
            sqlSession.rollback();
            throw new RuntimeException(e);
        } finally {
            sqlSession.close();

        }
    }

    /***
     * 입력한 아이디, 이름, 휴대폰번호를 갖고있는 유저를 찾는 함수
     * @param userDTO 입력한 아이디, 이름, 휴대폰번호
     * @return 일치하는 회원의 정보
     */
    public UserDTO existUserByInfo(UserDTO userDTO) {
        SqlSession sqlSession = getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        try {
            return userMapper.existUserByInfo(userDTO);
        } finally {
            sqlSession.close();
        }
    }


    public void addCart(String id, int choice, int count) {
        SqlSession sqlSession = getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int userSeq = userMapper.searchUser(id);
        CompanyMapper companyMapper = sqlSession.getMapper(CompanyMapper.class);
        Map<String, Integer> params = new HashMap<>();
        params.put("choice",choice);
        CartMapper cartMapper = sqlSession.getMapper(CartMapper.class);
        Integer comSeq = companyMapper.searchCom(params);

        List<CartDTO> cartList = new ArrayList<>();
        cartList = cartMapper.viewCart(userSeq);

        int num = 1;
        if (cartList != null && cartList.size() > 0) {
            for(CartDTO cartDTO : cartList) {
                if(cartDTO.getProductSeq() == choice){
                    num = 2;
                    count += cartDTO.getQuantity();
                    break;
                }
            }
        }

        if(num==1){
            Map<String, Object> map = new HashMap<>();

            map.put("productSeq", choice);
            map.put("userSeq", userSeq);
            map.put("companySeq", comSeq);
            map.put("quantity", count);
            int result = cartMapper.addCart(map);
            if(result > 0) {
                System.out.println("장바구니 등록 성공!");
                sqlSession.commit();
            } else {
                System.out.println("장바구니 등록 실패!");
                sqlSession.rollback();
            }
        }else{
            modifyQuantity(id,choice,count);
        }


        sqlSession.close();
    }

    public void viewCart(String id) {
        SqlSession sqlSession = getSqlSession();
        CartMapper cartMapper = sqlSession.getMapper(CartMapper.class);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        ProductMapper productMapper = sqlSession.getMapper(ProductMapper.class);
        int userSeq = userMapper.searchUser(id);
        List<CartDTO> cartList = new ArrayList<>();
        cartList = cartMapper.viewCart(userSeq);
        if(cartList != null && cartList.size() > 0) {
            for (CartDTO cartDTO : cartList) {
                String productName = productMapper.productName(cartDTO.getProductSeq());
                System.out.println("상품 번호 : "+ cartDTO.getProductSeq() +" 상품명 : " + productName  + " 수량 : "+ cartDTO.getQuantity());
            }
        }else{
            System.out.println("장바구니가 비어있습니다.");
        }
        sqlSession.close();
    }

    public void deleteAllCart(String id) {
        SqlSession sqlSession = getSqlSession();
        CartMapper cartMapper = sqlSession.getMapper(CartMapper.class);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int userSeq = userMapper.searchUser(id);
        int result = cartMapper.deleteAllCart(userSeq);
        if(result > 0) {
            sqlSession.commit();
        }else{
            System.out.println("카트 지우기 실패!");
        }
        sqlSession.close();
    }

    public void modifyQuantity(String id, int productNum, int quantity) {
        SqlSession sqlSession = getSqlSession();
        CartMapper cartMapper = sqlSession.getMapper(CartMapper.class);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int userSeq = userMapper.searchUser(id);
        Map<String, Integer> params = new HashMap<>();
        params.put("userSeq", userSeq);
        params.put("productNum", productNum);
        params.put("quantity", quantity);
        int result;
        if(quantity > 0) {
            result = cartMapper.modifyQuantity(params);
        }else{
            result = cartMapper.deleteQuantity(params);
        }

        if(result > 0) {
            System.out.println("물품수정 성공!");
            sqlSession.commit();
        }else{
            System.out.println("물품수정 실패!");
            sqlSession.rollback();
        }

        sqlSession.close();
    }

    public void addOrder(String id){
        SqlSession sqlSession = getSqlSession();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int userSeq = userMapper.searchUser(id);

        CartMapper cartMapper = sqlSession.getMapper(CartMapper.class);
        ProductMapper productMapper = sqlSession.getMapper(ProductMapper.class);
        List<CartDTO> cartList = new ArrayList<>();
        cartList = cartMapper.viewCart(userSeq);
        Map<String, Integer> params = new HashMap<>();
        if(cartList != null && cartList.size() > 0) {
            for (CartDTO cartDTO : cartList) {
                OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
                params.put("userSeq", userSeq);
                params.put("companySeq", cartDTO.getCompanySeq());

                Map<String, Integer> map = new HashMap<>();
                map.put("productSeq", cartDTO.getProductSeq());
                map.put("userSeq", userSeq);
                int price = productMapper.productPrice(map);

                params.put("totalPrice", price * cartDTO.getQuantity());
                int result = orderMapper.insertOrder(params);
                if(result > 0) {
                    sqlSession.commit();
                }
                else{
                    sqlSession.rollback();
                }
                int result1 = orderMapper.maxSeq();

                Map<String, Integer> map2 = new HashMap<>();
                map2.put("cartSeq", cartDTO.getCartSeq());
                map2.put("orderSeq", result1);
                int rresult = cartMapper.updateOrderSeq(map2);

                if(rresult > 0) {
                    sqlSession.commit();
                }else{
                    sqlSession.rollback();
                }
            }
        }else{
            System.out.println("결재할 상품이 없습니다.");
        }


        sqlSession.close();
    }

    public void paymentCart(String id) {
        SqlSession sqlSession = getSqlSession();
        CartMapper cartMapper = sqlSession.getMapper(CartMapper.class);
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int userSeq = userMapper.searchUser(id);
        Map<String, Integer> params = new HashMap<>();
        params.put("userSeq", userSeq);

        int result = cartMapper.modifyYN(params);

        if(result > 0) {
            System.out.println("결제가 완료되었습니다.");
            sqlSession.commit();
        }else{
            sqlSession.rollback();
        }
        sqlSession.close();

    }

    public void viewOrder(String id) {
        SqlSession sqlSession = getSqlSession();
        OrderMapper orderMapper = sqlSession.getMapper(OrderMapper.class);
        List<OrderDTO> orderList = new ArrayList<>();
        UserMapper userMapper = sqlSession.getMapper(UserMapper.class);
        int userSeq = userMapper.searchUser(id);

        orderList = orderMapper.allOrder(userSeq);
        if(orderList != null && orderList.size() > 0) {
            for (OrderDTO orderDTO : orderList) {
                System.out.println(orderDTO.toString());
            }
        }else{
            System.out.println("결재 내역이 없습니다.");
        }
        sqlSession.close();
    }
}
