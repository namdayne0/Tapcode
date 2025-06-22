// src/ticket/close.js
const { SlashCommandBuilder, PermissionsBitField, EmbedBuilder, ButtonBuilder, ButtonStyle, ActionRowBuilder } = require('discord.js');

module.exports = {
    data: new SlashCommandBuilder()
        .setName('close-ticket')
        .setDescription('Đóng ticket hỗ trợ hiện tại (chỉ dùng nút bấm).'),
    
    async execute(interaction) {
        // Nếu đây là tương tác từ slash command, trả lời và hướng dẫn dùng nút bấm
        if (interaction.isChatInputCommand()) {
            return interaction.reply({ 
                content: 'Vui lòng sử dụng nút "Đóng Ticket" trong kênh ticket để đóng ticket.', 
                ephemeral: true 
            });
        }

        // --- Xử lý đóng ticket từ button ---
        if (interaction.isButton() && interaction.customId === 'close_ticket') {
            const channel = interaction.channel;
            const member = interaction.member;

            if (!channel.name.startsWith('ticket-')) {
                return interaction.reply({ content: 'Lệnh này chỉ có thể được sử dụng trong một kênh ticket.', ephemeral: true, ephemeral: true });
            }

            // Lấy ID của người tạo ticket từ topic của kênh
            const creatorIdMatch = channel.topic ? channel.topic.match(/ID: (\d+)/) : null;
            const creatorId = creatorIdMatch ? creatorIdMatch[1] : null;

            // Kiểm tra quyền: người tạo ticket (dựa vào topic) hoặc quản trị viên/người có quyền ManageChannels
            const isCreator = creatorId && member.id === creatorId;
            const hasPermission = isCreator ||
                                  member.permissions.has(PermissionsBitField.Flags.ManageChannels) ||
                                  member.permissions.has(PermissionsBitField.Flags.Administrator);

            if (!hasPermission) {
                return interaction.reply({ content: 'Bạn không có quyền đóng ticket này.', ephemeral: true });
            }

            // Tạm thời trả lời để tránh timeout, tin nhắn này sẽ bị thay thế hoặc xóa
            await interaction.reply({ content: `Ticket này sẽ đóng trong 5 giây...`, ephemeral: false }); 

            // Gửi tin nhắn thông báo trước khi xóa kênh
            const closeEmbed = new EmbedBuilder()
                .setColor(0xFF0000) // Màu đỏ
                .setTitle('Ticket Đã Đóng')
                .setDescription(`Ticket này đã được đóng bởi ${member.user.tag}.\n\nKênh này sẽ bị xóa tự động.`)
                .setFooter({ text: 'Cảm ơn bạn đã liên hệ!', iconURL: interaction.client.user.displayAvatarURL() })
                .setTimestamp();

            await channel.send({ embeds: [closeEmbed] });

            // Xóa kênh sau một khoảng thời gian ngắn
            setTimeout(async () => {
                try {
                    await channel.delete();
                    console.log(`Kênh ticket ${channel.name} đã bị xóa bởi ${member.user.tag}.`);
                } catch (error) {
                    console.error(`Lỗi khi xóa kênh ticket ${channel.name}:`, error);
                    // Nếu kênh đã bị xóa hoặc có lỗi, không cần làm gì thêm
                }
            }, 5000); // Xóa sau 5 giây
        }
    },
};