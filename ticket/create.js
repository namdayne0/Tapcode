// src/ticket/create.js
const { SlashCommandBuilder, ChannelType, PermissionsBitField, EmbedBuilder, ActionRowBuilder, ButtonBuilder, ButtonStyle } = require('discord.js');

module.exports = {
    data: new SlashCommandBuilder()
        .setName('create-ticket')
        .setDescription('Tạo một ticket hỗ trợ mới (chỉ dùng nút bấm).'), // Đổi mô tả để chỉ rõ dùng nút bấm
    
    async execute(interaction) {
        // Nếu đây là tương tác từ slash command, trả lời và hướng dẫn dùng nút bấm
        if (interaction.isChatInputCommand()) {
            return interaction.reply({ 
                content: 'Vui lòng sử dụng nút "Tạo Ticket Mới" từ tin nhắn thiết lập ticket để tạo ticket.', 
                ephemeral: true 
            });
        }

        // --- Xử lý tạo ticket từ button ---
        if (interaction.isButton() && interaction.customId === 'create_ticket') {
            await interaction.deferReply({ ephemeral: true }); // Tạm hoãn phản hồi để tránh lỗi timeout

            const guild = interaction.guild;
            const member = interaction.member;

            const username = member.user.username.toLowerCase().replace(/[^a-z0-9]/g, '');
            const ticketChannelName = `ticket-${username}-${Date.now().toString().slice(-4)}`;

            // Tìm hoặc tạo category "Tickets"
            let ticketCategory = guild.channels.cache.find(
                c => c.name === 'Tickets' && c.type === ChannelType.GuildCategory
            );

            if (!ticketCategory) {
                try {
                    ticketCategory = await guild.channels.create({
                        name: 'Tickets',
                        type: ChannelType.GuildCategory,
                        permissionOverwrites: [
                            {
                                id: guild.id, // @everyone
                                deny: [PermissionsBitField.Flags.ViewChannel],
                            },
                        ],
                    });
                    console.log(`Đã tạo category "Tickets": ${ticketCategory.name}`);
                } catch (error) {
                    console.error('Lỗi khi tạo category "Tickets":', error);
                    return interaction.editReply({ 
                        content: 'Có lỗi xảy ra khi tạo category ticket. Vui lòng đảm bảo bot có quyền "Quản lý Kênh".', 
                        ephemeral: true 
                    });
                }
            }

            // Kiểm tra xem người dùng đã có ticket đang mở chưa trong category này
            const existingTicket = guild.channels.cache.find(
                c => c.name.startsWith(`ticket-${username}`) && c.parentId === ticketCategory.id && 
                     c.topic && c.topic.includes(`ID: ${member.id}`) // Kiểm tra thêm ID người tạo trong topic
            );
            if (existingTicket) {
                return interaction.editReply({ 
                    content: `Bạn đã có một ticket đang mở tại ${existingTicket}. Vui lòng sử dụng ticket đó hoặc đóng nó trước khi tạo ticket mới.`, 
                    ephemeral: true 
                });
            }

            // Tạo kênh ticket mới
            let newTicketChannel;
            try {
                newTicketChannel = await guild.channels.create({
                    name: ticketChannelName,
                    type: ChannelType.GuildText,
                    parent: ticketCategory.id,
                    permissionOverwrites: [
                        {
                            id: member.id, // Cho phép người tạo ticket xem kênh
                            allow: [
                                PermissionsBitField.Flags.ViewChannel,
                                PermissionsBitField.Flags.SendMessages,
                                PermissionsBitField.Flags.ReadMessageHistory,
                            ],
                        },
                        {
                            id: guild.id, // @everyone không thể xem kênh này
                            deny: [PermissionsBitField.Flags.ViewChannel],
                        },
                        // Thêm các role hỗ trợ hoặc admin có thể xem ticket ở đây
                        // Ví dụ: { id: 'ID_CUA_ROLE_SUPPORTER', allow: [PermissionsBitField.Flags.ViewChannel] },
                    ],
                    topic: `Ticket được tạo bởi người dùng có ID: ${member.id}.` // Lưu ID người tạo vào topic để kiểm tra quyền đóng ticket
                });
            } catch (error) {
                console.error('Lỗi khi tạo kênh ticket mới:', error);
                return interaction.editReply({ 
                    content: 'Có lỗi xảy ra khi tạo kênh ticket. Vui lòng đảm bảo bot có quyền "Quản lý Kênh".', 
                    ephemeral: true 
                });
            }

            const closeTicketButton = new ButtonBuilder()
                .setCustomId('close_ticket')
                .setLabel('Đóng Ticket')
                .setStyle(ButtonStyle.Danger) // Màu đỏ
                .setEmoji('🔒');

            const row = new ActionRowBuilder()
                .addComponents(closeTicketButton);

            const ticketEmbed = new EmbedBuilder()
                .setColor(0x00FF00) // Màu xanh lá
                .setTitle(`🎫 Ticket Hỗ Trợ của ${member.user.tag}`)
                .setDescription(`Chào mừng bạn đến với ticket hỗ trợ của bạn!\n\n**Vui lòng mô tả chi tiết vấn đề bạn đang gặp phải tại đây.**\n\nKhi vấn đề của bạn đã được giải quyết, hãy nhấn nút "Đóng Ticket" bên dưới.`)
                .setAuthor({ name: member.user.tag, iconURL: member.user.displayAvatarURL() })
                .setFooter({ text: 'Cảm ơn bạn đã liên hệ!', iconURL: interaction.client.user.displayAvatarURL() })
                .setTimestamp();

            await newTicketChannel.send({
                content: `${member}, đội ngũ hỗ trợ sẽ sớm phản hồi bạn!`,
                embeds: [ticketEmbed],
                components: [row]
            });

            await interaction.editReply({
                content: `Ticket của bạn đã được tạo tại ${newTicketChannel}. Vui lòng chuyển đến kênh đó để tiếp tục.`,
                ephemeral: true
            });

            console.log(`Ticket mới đã được tạo bởi ${member.user.tag} trong kênh ${newTicketChannel.name}.`);
        }
    },
};