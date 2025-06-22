// Yêu cầu các module cần thiết
const { Client, Collection, GatewayIntentBits } = require('discord.js');
const dotenv = require('dotenv');
const fs = require('node:fs');
const path = require('node:path');

// Tải biến môi trường từ file .env
dotenv.config();

// Tạo một Discord client mới với các intent cần thiết
const client = new Client({
    intents: [
        GatewayIntentBits.Guilds,           // Cần để xử lý tương tác lệnh và thông tin guild
        GatewayIntentBits.GuildMessages,    // Cần để đọc tin nhắn trong guild
        GatewayIntentBits.MessageContent,   // Cần để truy cập nội dung tin nhắn (nếu bạn có xử lý tin nhắn thường)
        GatewayIntentBits.DirectMessages,   // Cần nếu bạn muốn bot hoạt động trong DM
        GatewayIntentBits.GuildMembers,     // Cần để lấy thông tin thành viên (ví dụ: cho ticket)
        GatewayIntentBits.GuildMessageReactions, // Cần cho các tương tác nút (button) nếu có
        // Thêm các intent khác nếu bot của bạn cần
    ],
});

// Tạo một Collection để lưu trữ các lệnh của bot
client.commands = new Collection();

// --- Tải lệnh (Commands) từ src/commands ---
const commandsPath = path.join(__dirname, 'commands'); // Đường dẫn đến thư mục commands
const commandFiles = fs.readdirSync(commandsPath).filter(file => file.endsWith('.js'));

for (const file of commandFiles) {
    const filePath = path.join(commandsPath, file);
    try {
        const command = require(filePath);
        // Kiểm tra xem lệnh có thuộc tính 'data' và 'execute' không
        if ('data' in command && 'execute' in command) {
            client.commands.set(command.data.name, command);
            console.log(`✅ Đã tải lệnh: "${command.data.name}" từ ${file}`);
        } else {
            console.warn(`[WARNING] Lệnh tại ${filePath} thiếu thuộc tính "data" hoặc "execute".`);
        }
    } catch (error) {
        console.error(`❌ Lỗi khi tải lệnh từ ${file}:`, error);
    }
}

// --- Tải lệnh (Commands) từ thư mục ticket ---
const ticketCommandsPath = path.join(__dirname, '..', 'ticket'); // Đường dẫn đến thư mục ticket (ở cùng cấp với src)
const ticketCommandFiles = fs.readdirSync(ticketCommandsPath).filter(file => file.endsWith('.js'));

for (const file of ticketCommandFiles) {
    const filePath = path.join(ticketCommandsPath, file);
    try {
        const command = require(filePath);
        if ('data' in command && 'execute' in command) {
            client.commands.set(command.data.name, command);
            console.log(`✅ Đã tải lệnh: "${command.data.name}" từ ticket/${file}`); // Để dễ nhận biết trong log
        } else {
            console.warn(`[WARNING] Lệnh ticket tại ${filePath} thiếu thuộc tính "data" hoặc "execute".`);
        }
    } catch (error) {
        console.error(`❌ Lỗi khi tải lệnh ticket từ ${file}:`, error);
    }
}

// --- Tải sự kiện (Events) ---
const eventsPath = path.join(__dirname, 'events'); // Đường dẫn đến thư mục events
const eventFiles = fs.readdirSync(eventsPath).filter(file => file.endsWith('.js'));

for (const file of eventFiles) {
    const filePath = path.join(eventsPath, file);
    try {
        const event = require(filePath);
        // Kiểm tra xem sự kiện có thuộc tính 'name' và 'execute' không
        if ('name' in event && 'execute' in event) {
            if (event.once) { // Nếu sự kiện chỉ chạy một lần
                client.once(event.name, (...args) => event.execute(...args, client));
            } else { // Nếu sự kiện chạy nhiều lần
                client.on(event.name, (...args) => event.execute(...args, client));
            }
            console.log(`✅ Đã tải sự kiện: "${event.name}" từ ${file}`);
        } else {
            console.warn(`[WARNING] Sự kiện tại ${filePath} thiếu thuộc tính "name" hoặc "execute".`);
        }
    } catch (error) {
        console.error(`❌ Lỗi khi tải sự kiện từ ${file}:`, error);
    }
}

// Xử lý tương tác lệnh (Slash Commands) và tương tác nút (Button Interactions)
client.on('interactionCreate', async interaction => {
    if (interaction.isChatInputCommand()) { // Xử lý Slash Commands
        const command = client.commands.get(interaction.commandName);

        if (!command) {
            console.error(`Không tìm thấy lệnh "${interaction.commandName}".`);
            return interaction.reply({ content: 'Lệnh này không tồn tại!', ephemeral: true });
        }

        try {
            await command.execute(interaction);
        } catch (error) {
            console.error(`Lỗi khi thực thi lệnh "${interaction.commandName}":`, error);
            if (interaction.deferred || interaction.replied) {
                await interaction.followUp({ content: 'Đã có lỗi xảy ra khi thực hiện lệnh này!', ephemeral: true });
            } else {
                await interaction.reply({ content: 'Đã có lỗi xảy ra khi thực hiện lệnh này!', ephemeral: true });
            }
        }
    } else if (interaction.isButton()) { // Xử lý Button Interactions
        if (interaction.customId === 'create_ticket') {
            const createTicketCommand = client.commands.get('create-ticket'); // Lấy lệnh create-ticket
            if (createTicketCommand) {
                try {
                    await createTicketCommand.execute(interaction); // Thực thi lệnh create-ticket
                } catch (error) {
                    console.error('Lỗi khi thực thi button create_ticket:', error);
                    if (interaction.deferred || interaction.replied) {
                        await interaction.followUp({ content: 'Đã có lỗi xảy ra khi tạo ticket!', ephemeral: true });
                    } else {
                        await interaction.reply({ content: 'Đã có lỗi xảy ra khi tạo ticket!', ephemeral: true });
                    }
                }
            } else {
                console.warn('Lệnh create-ticket không được tìm thấy khi nút được nhấn.');
                await interaction.reply({ content: 'Không tìm thấy lệnh tạo ticket. Vui lòng báo cho quản trị viên.', ephemeral: true });
            }
        }
        // Thêm các xử lý button khác nếu có
    }
});

// Đăng nhập bot Discord
client.login(process.env.DISCORD_BOT_TOKEN)
    .then(() => console.log('Bot đang đăng nhập...'))
    .catch(error => console.error('Lỗi khi đăng nhập bot:', error));