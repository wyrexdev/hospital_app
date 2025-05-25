const users = 
`CREATE TABLE hospitalapp.users (
    id UUID PRIMARY KEY,
    current_session_quest UUID,
    user_hmac TEXT,
    username TEXT,
    name TEXT,
    surname TEXT,
    tc TEXT,
    birth_date INT,
    gender INT,
    chronic_diseases TEXT,
    allergies TEXT,
    disability_status INT,
    insurance_type INT,
    address TEXT,
    email TEXT,
    password TEXT,
    profile_picture TEXT,
    sign_date TIMESTAMP,
    last_login_ip TEXT,
    is_active INT,
    user_type INT,
    medical_specialties INT
);`;

const appointments = 
`CREATE TABLE hospitalapp.appointments (
    id UUID PRIMARY KEY,
    user_id UUID,
    created_at TIMESTAMP,
    doctor_id UUID,
    status INT,
    appointment_date TEXT,
    appointment_time TEXT,
    description TEXT
);`;

const hospitals = 
`CREATE TABLE hospitalapp.hospitals (
    id UUID PRIMARY KEY,
    hospital_name TEXT,
    hospital_adress TEXT,
    contact_info TEXT,
    hospital_type INT,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    author UUID
);`;

const platforms = 
`CREATE TABLE hospitalapp.platforms (
    id UUID PRIMARY KEY,
    platform INT,
    user_id UUID
);`;

const verify = 
`CREATE TABLE hospitalapp.verify (
    id UUID PRIMARY KEY,
    quest_id UUID,
    ip TEXT,
    expire TIMESTAMP,
    date TIMESTAMP,
    is_used INT 
);`;

const quests = 
`CREATE TABLE hospitalapp.quests (
    id UUID PRIMARY KEY,
    user_id UUID,
    quest_time TIMESTAMP,
    ip_adress TEXT,
    is_verify INT
)`;

const hospitalApplies = `CREATE TABLE hospitalapp.hospital_applies (
    id UUID PRIMARY KEY,
    hospital_name TEXT,
    hospital_address TEXT,
    hospital_phone TEXT,
    hospital_email TEXT,
    hospital_type INT,
    author UUID,
    hospital_image_url TEXT,
    created_at TIMESTAMP
)`;

const rooms = `CREATE TABLE hospitalapp.rooms (
    id UUID PRIMARY KEY,
    author UUID,
    to_user UUID,
    last_message TEXT,
    last_message_date TIMESTAMP,
    created_at TIMESTAMP
)`;

const doctors = `CREATE TABLE hopsitalapp.doctors (
    id UUID PRIMARY KEY,
    author UUID
)`;

module.exports = {
    users,
    hospitals,
    appointments,
    platforms,
    verify,
    quests,
    hospitalApplies,
    rooms,
    doctors
}