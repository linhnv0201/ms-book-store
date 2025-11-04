package user_service.user.mapper;

import org.mapstruct.BeanMapping;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.mapstruct.NullValuePropertyMappingStrategy;
import user_service.user.dto.UserInfoUpdateRequest;
import user_service.user.dto.UserResponse;
import user_service.user.dto.UserResponseForAuthentication;
import user_service.user.dto.UserUpdateRequest;
import user_service.user.entity.User;

//Báo cho MapStruct biết đây là mapper interface.
//componentModel = "spring" → MapStruct sẽ generate 1 implementation class và annotate nó
//  với @Component, để Spring quản lý bean này.
//Nhờ đó, bạn có thể @Autowired hoặc @RequiredArgsConstructor để inject UserMapper vào service.
@Mapper(componentModel = "spring")
public interface UserMapper {
    User toCreateUser(user_service.user.dto.UserCreationRequest request);
    UserResponse toUserResponse(User user);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toUpdateUser(@MappingTarget User user, UserUpdateRequest request);
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void toUpdateUserInfo(@MappingTarget User user, UserInfoUpdateRequest request);
    //Bình thường, MapStruct sẽ tạo mới object khi map từ DTO → Entity.
    //Nhưng khi bạn muốn cập nhật một object đã có sẵn (ví dụ User lấy từ database), bạn cần nói cho MapStruct biết là:
    //👉 “Hãy map các field từ DTO vào object này, đừng tạo mới.”
    //Annotation @MappingTarget chính là để làm việc đó.
}
