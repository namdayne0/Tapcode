// src/events/duytribot.js
module.exports = {
    name: 'ready', // Tên sự kiện Discord mà chúng ta muốn lắng nghe (bot sẵn sàng)
    once: true,    // Đặt là 'true' để sự kiện này chỉ chạy một lần khi bot khởi động

    // Hàm execute sẽ được gọi khi sự kiện 'ready' xảy ra
    // 'client' là đối tượng Discord client của bot
    execute(client) {
        // Lấy khoảng thời gian (tính bằng giây) từ biến môi trường LOG_INTERVAL_SECONDS
        // Nếu biến này không tồn tại hoặc không hợp lệ, mặc định là 300 giây (5 phút)
        const LOG_INTERVAL = parseInt(process.env.LOG_INTERVAL_SECONDS || '300') * 1000; // Chuyển đổi sang milliseconds

        // Thiết lập một bộ hẹn giờ chạy lặp lại
        setInterval(() => {
            const now = new Date(); // Lấy thời gian hiện tại
            // In thông báo vào console
            console.log(`[DUY TRÌ BOT] Bot đang hoạt động! - ${now.toLocaleString('vi-VN')}`);
        }, LOG_INTERVAL); // Khoảng thời gian giữa các lần in

        // Thông báo rằng chức năng duy trì bot đã được khởi tạo
        console.log(`[DUY TRÌ BOT] Chức năng duy trì bot (log console) đã được khởi tạo. Sẽ log mỗi ${LOG_INTERVAL / 1000} giây.`);
    },
};