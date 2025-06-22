const { SlashCommandBuilder, EmbedBuilder } = require('discord.js');
const util = require('minecraft-server-util'); // Import gói minecraft-server-util

module.exports = {
    // Định nghĩa Slash Command
    data: new SlashCommandBuilder()
        .setName('server')
        .setDescription('Kiểm tra trạng thái của máy chủ Minecraft.')
        .addStringOption(option =>
            option.setName('ip')
                .setDescription('Địa chỉ IP hoặc tên miền của máy chủ Minecraft (ví dụ: play.hypixel.net)')
                .setRequired(true)), // Bắt buộc phải có IP

    // Hàm thực thi khi lệnh được gọi
    async execute(interaction) {
        await interaction.deferReply(); // Tạm thời hoãn phản hồi vì có thể mất chút thời gian để ping server

        const ip = interaction.options.getString('ip');
        const defaultPort = 25565; // Cổng mặc định của Minecraft

        try {
            // Tách IP và cổng nếu người dùng nhập dạng "ip:port"
            let host = ip;
            let port = defaultPort;

            if (ip.includes(':')) {
                const parts = ip.split(':');
                host = parts[0];
                port = parseInt(parts[1], 10);
                if (isNaN(port)) {
                    port = defaultPort; // Nếu cổng không hợp lệ, dùng cổng mặc định
                }
            }

            // Ping máy chủ Minecraft
            const result = await util.status(host, port, { timeout: 5000 }); // Thêm timeout 5 giây

            // Tạo Embed hiển thị thông tin
            const serverEmbed = new EmbedBuilder()
                .setColor(0x00FF00) // Màu xanh lá cây
                .setTitle(`🌍 Trạng Thái Máy Chủ Minecraft: \`${host}:${port}\``)
                .setThumbnail('https://cdn.discordapp.com/attachments/YOUR_ATTACHMENT_ID/MINECRAFT_LOGO.png?ex=666eb994&is=666d6814&hm=45f0d922a96752396e95123512e84841961e6c3fef04e0e4b85c1926639d679d&') // Thay bằng URL icon Minecraft đẹp mắt nếu có
                .addFields(
                    { name: 'Trạng Thái', value: '🟢 **ONLINE**', inline: true },
                    { name: 'Ping', value: `${result.ping}ms`, inline: true },
                    { name: '\u200B', value: '\u200B', inline: true }, // Khoảng trống
                    { name: 'Phiên bản', value: `\`${result.version.name_clean || 'N/A'}\``, inline: true },
                    { name: 'Người chơi', value: `\`${result.players.online}/${result.players.max}\``, inline: true },
                    { name: '\u200B', value: '\u200B', inline: true }, // Khoảng trống
                )
                .setTimestamp()
                .setFooter({ text: `Yêu cầu bởi ${interaction.user.tag}` });

            // Kiểm tra và thêm TPS nếu có
            if (result.tps) { // Một số server (Spigot/Paper) có thể trả về TPS
                serverEmbed.addFields(
                    { name: 'TPS (Ticks Per Second)', value: `\`${result.tps.toFixed(2)}\``, inline: true }
                );
            }

            // Thêm MOTD (Message of the Day) của server
            if (result.motd.clean) {
                serverEmbed.setDescription(`*MOTD: ${result.motd.clean.substring(0, 200)}${result.motd.clean.length > 200 ? '...' : ''}*`);
            }
            
            await interaction.editReply({ embeds: [serverEmbed] }); // Chỉnh sửa phản hồi ban đầu
        } catch (error) {
            console.error(`Lỗi khi ping server ${ip}:`, error);
            const errorEmbed = new EmbedBuilder()
                .setColor(0xFF0000) // Màu đỏ
                .setTitle('Máy Chủ Minecraft')
                .setDescription(`❌ Không thể kết nối hoặc tìm thấy máy chủ \`${ip}\`.`)
                .addFields(
                    { name: 'Lý do có thể', value: '1. Máy chủ offline\n2. Sai địa chỉ IP/cổng\n3. Máy chủ chặn ping' }
                )
                .setFooter({ text: `Yêu cầu bởi ${interaction.user.tag}` })
                .setTimestamp();
            await interaction.editReply({ embeds: [errorEmbed] }); // Chỉnh sửa phản hồi lỗi
        }
    },
};