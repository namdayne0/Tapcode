const { SlashCommandBuilder, EmbedBuilder } = require('discord.js');

module.exports = {
    // Định nghĩa Slash Command
    data: new SlashCommandBuilder()
        .setName('say')
        .setDescription('Khiến bot nói một tin nhắn trong kênh.')
        .addStringOption(option =>
            option.setName('nội_dung')
                .setDescription('Nội dung tin nhắn mà bot sẽ nói.')
                .setRequired(true)) // Bắt buộc phải có nội dung tin nhắn
        .addAttachmentOption(option =>
            option.setName('ảnh')
                .setDescription('Tệp ảnh bạn muốn bot gửi cùng tin nhắn.'))
        .addBooleanOption(option =>
            option.setName('nhúng')
                .setDescription('Chọn TRUE để bot gửi tin nhắn dạng embed (nhúng).')),

    // Hàm thực thi khi lệnh được gọi
    async execute(interaction) {
        const messageContent = interaction.options.getString('nội_dung');
        const imageAttachment = interaction.options.getAttachment('ảnh');
        const useEmbed = interaction.options.getBoolean('nhúng');

        try {
            if (useEmbed) {
                // Tạo một embed builder
                const embed = new EmbedBuilder()
                    .setColor(0x0099FF) // Màu xanh dương
                    .setTitle('Thông báo từ Bot')
                    .setDescription(messageContent || 'Không có nội dung tin nhắn.') // Đảm bảo có nội dung
                    .setTimestamp() // Thêm thời gian
                    .setFooter({ text: `Yêu cầu bởi ${interaction.user.username}`, iconURL: interaction.user.displayAvatarURL() });

                if (imageAttachment) {
                    embed.setImage(imageAttachment.url); // Đặt ảnh vào embed
                }

                await interaction.channel.send({ embeds: [embed] });

            } else {
                // Xử lý xuống dòng: Thay thế '\n' trong nội dung tin nhắn
                const formattedContent = messageContent.replace(/\\n/g, '\n');

                const sendOptions = { content: formattedContent };

                if (imageAttachment) {
                    sendOptions.files = [{ attachment: imageAttachment.url, name: imageAttachment.name }];
                }

                await interaction.channel.send(sendOptions);
            }

            // Gửi phản hồi ephemeral cho người dùng biết lệnh đã thành công
            await interaction.reply({ content: 'Đã gửi tin nhắn!', ephemeral: true });

        } catch (error) {
            console.error('Lỗi khi gửi tin nhắn:', error);
            await interaction.reply({ content: 'Đã có lỗi xảy ra khi cố gắng gửi tin nhắn!', ephemeral: true });
        }
    },
};