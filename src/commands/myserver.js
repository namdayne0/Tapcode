const { SlashCommandBuilder, AttachmentBuilder, EmbedBuilder } = require('discord.js');
const path = require('path'); // Import module path để xử lý đường dẫn file

module.exports = {
    // Định nghĩa Slash Command
    data: new SlashCommandBuilder()
        .setName('myserver')
        .setDescription('Hiển thị thông tin máy chủ của bạn bằng một hình ảnh.'),

    // Hàm thực thi khi lệnh được gọi
    async execute(interaction) {
        // Tên file ảnh trong thư mục assets
        const imageName = 'my_server_image.png'; // <-- THAY THẾ BẰNG TÊN FILE ẢNH CỦA BẠN!
        const imagePath = path.join(__dirname, '..', 'assets', imageName); // Đường dẫn đến ảnh

        try {
            // Tạo một Attachment từ file ảnh
            const file = new AttachmentBuilder(imagePath, { name: imageName });

            // Tạo Embed để chứa ảnh và thông tin
            const embed = new EmbedBuilder()
                .setColor(0x00FFFF) // Màu xanh ngọc
                .setTitle('Thông Tin Máy Chủ Của Tôi')
                .setDescription('Đây là một số hình ảnh hoặc thông tin liên quan đến máy chủ của bạn!')
                .setImage(`attachment://${imageName}`) // Đặt ảnh vào Embed
                .setTimestamp()
                .setFooter({ text: `Yêu cầu bởi ${interaction.user.tag}` });

            // Gửi tin nhắn chứa Embed và Attachment
            await interaction.reply({ embeds: [embed], files: [file] });

        } catch (error) {
            console.error(`Lỗi khi gửi ảnh ${imageName}:`, error);
            await interaction.reply({ content: `Xin lỗi, không thể tìm thấy hoặc gửi hình ảnh \`${imageName}\`. Vui lòng kiểm tra tên file và thư mục.`, ephemeral: true });
        }
    },
};