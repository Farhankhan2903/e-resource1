import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';

const JWT_SECRET = process.env.JWT_SECRET || 'er_enterprise_secret_2024';

export const hashPassword = async (password) => {
    return await bcrypt.hash(password, 12);
};

export const comparePassword = async (password, hash) => {
    return await bcrypt.compare(password, hash);
};

export const generateAccessToken = (user) => {
    return jwt.sign(
        { id: user.user_id, email: user.email_id, role: user.role },
        JWT_SECRET,
        { expiresIn: '1h' }
    );
};

export const generateRefreshToken = (user) => {
    return jwt.sign(
        { id: user.user_id },
        JWT_SECRET,
        { expiresIn: '7d' }
    );
};

export const authenticateRequest = (req) => {
    const authHeader = req.headers.authorization;
    if (!authHeader) return null;

    try {
        const token = authHeader.split(' ')[1];
        return jwt.verify(token, JWT_SECRET);
    } catch (err) {
        return null;
    }
};
