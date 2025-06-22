// src/ticket/setup.js
const { SlashCommandBuilder, PermissionsBitField, EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle } = require('discord.js');

// VUI LÒNG THAY THẾ ID NÀY BẰNG ID DISCORD CỦA BẠN
// Chỉ người dùng có ID này mới có thể sử dụng lệnh /ticket-setup
const AUTHORIZED_USER_ID = '1329263302337105994'; 

module.exports = {
    // Định nghĩa lệnh slash command /ticket-setup
    data: new SlashCommandBuilder()
        .setName('ticket-setup')
        .setDescription('Thiết lập kênh tạo ticket với tin nhắn embed và nút bấm.')
        .addChannelOption(option =>
            option.setName('kênh')
                .setDescription('Kênh để gửi tin nhắn thiết lập ticket.')
                .setRequired(true)),
    
    // Hàm thực thi khi lệnh được gọi
    async execute(interaction) {
        // 1. Kiểm tra quyền của người dùng (chỉ người có ID cụ thể mới được dùng lệnh này)
        if (interaction.user.id !== AUTHORIZED_USER_ID) {
            return interaction.reply({ 
                content: 'Bạn không có quyền sử dụng lệnh này. Chỉ người dùng có ID đã cấu hình mới có thể chạy lệnh này.', 
                ephemeral: true // Chỉ người dùng thấy tin nhắn này
            });
        }

        const targetChannel = interaction.options.getChannel('kênh');

        // 2. Kiểm tra kênh được chọn có phải là kênh văn bản không
        if (!targetChannel.isTextBased()) {
            return interaction.reply({ 
                content: 'Kênh được chọn phải là kênh văn bản.', 
                ephemeral: true 
            });
        }
        
        // 3. Kiểm tra quyền của bot trong kênh đích
        const botPermissionsInChannel = targetChannel.guild.members.me.permissionsIn(targetChannel);
        if (!botPermissionsInChannel.has(PermissionsBitField.Flags.SendMessages) || 
            !botPermissionsInChannel.has(PermissionsBitField.Flags.EmbedLinks)) {
            return interaction.reply({ 
                content: `Bot không có đủ quyền để gửi tin nhắn hoặc gửi embed trong kênh ${targetChannel}. Vui lòng cấp quyền "Gửi tin nhắn" và "Nhúng liên kết" cho bot.`, 
                ephemeral: true 
            });
        }

        // Tạm hoãn phản hồi để bot có thời gian gửi tin nhắn setup
        await interaction.reply({ content: `Đang thiết lập tin nhắn ticket trong kênh ${targetChannel}...`, ephemeral: true });

        // 4. Tạo Embed (tin nhắn nhúng) đẹp mắt
        const setupEmbed = new EmbedBuilder()
            .setColor(0x0099FF) // Màu xanh dương
            .setTitle('🎫 Hệ thống Ticket Hỗ trợ')
            .setDescription('Chào mừng bạn đến với hệ thống ticket của chúng tôi! Để nhận được hỗ trợ nhanh chóng và hiệu quả, vui lòng nhấn vào nút bên dưới để tạo một ticket mới. Đội ngũ hỗ trợ của chúng tôi sẽ sớm liên hệ với bạn để giải quyết vấn đề.')
            .addFields(
                { name: '✨ Dễ dàng và Thuận tiện', value: 'Chỉ cần một cú nhấp chuột để bắt đầu quá trình hỗ trợ.', inline: true },
                { name: '💬 Phản hồi nhanh chóng', value: 'Chúng tôi cam kết đội ngũ hỗ trợ sẽ phản hồi bạn trong thời gian sớm nhất.', inline: true },
                { name: '🔒 Riêng tư và Bảo mật', value: 'Mỗi ticket là một kênh chat riêng tư, đảm bảo thông tin của bạn được bảo mật.', inline: false },
                { name: '💡 Hướng dẫn', value: 'Sau khi tạo ticket, hãy mô tả chi tiết vấn đề của bạn trong kênh mới được tạo.', inline: false }
            )
            // HÌNH ẢNH BANNER CỦA BẠN: VUI LÒNG THAY THẾ URL NÀY
            // Bạn cần tải ảnh của mình lên một dịch vụ lưu trữ ảnh (ví dụ: Imgur, hoặc tải lên Discord rồi copy link ảnh)
            // Đây là một URL ảnh ví dụ. Hãy thay thế nó!
            .setImage('https://i.pinimg.com/736x/09/e7/67/09e767918ea567d8d23845a94f55fea4.jpg') 
            .setThumbnail(interaction.guild.iconURL() || null) // Lấy icon của máy chủ
            .setFooter({ 
                text: `Hệ thống Ticket của ${interaction.guild.name}`, 
                iconURL: interaction.client.user.displayAvatarURL() // Icon của bot
            })
            .setTimestamp(); // Hiển thị thời gian gửi tin nhắn

        // 5. Tạo Nút bấm "Tạo Ticket Mới"
        const createTicketButton = new ButtonBuilder()
            .setCustomId('create_ticket') // ID duy nhất để bot nhận biết nút này
            .setLabel('Tạo Ticket Mới')
            .setStyle(ButtonStyle.Primary) // Màu xanh dương đậm
            .setEmoji('➕'); // Emoji cho nút bấm

        const row = new ActionRowBuilder()
            .addComponents(createTicketButton); // Thêm nút vào hàng hành động

        // 6. Gửi tin nhắn embed và nút bấm vào kênh đích
        await targetChannel.send({
            embeds: [setupEmbed],
            components: [row]
        });

        // 7. Gửi phản hồi cuối cùng cho người dùng đã dùng lệnh /ticket-setup
        await interaction.editReply({ content: `✅ Đã thiết lập hệ thống ticket trong kênh ${targetChannel} thành công!`, ephemeral: true });
    },
};