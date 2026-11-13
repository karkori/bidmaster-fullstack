import { z } from 'zod';

export const ALLOWED_IMAGE_TYPES = [
  'image/png',
  'image/jpeg',
  'image/jpg',
  'image/webp',
  'image/gif',
] as const;

export const MAX_IMAGE_SIZE_BYTES = 5 * 1024 * 1024; // 5 MB

export function validateImageFile(file: File): string | null {
  if (!ALLOWED_IMAGE_TYPES.includes(file.type.toLowerCase() as (typeof ALLOWED_IMAGE_TYPES)[number])) {
    return `Formato no soportado (${file.name}). Usa PNG, JPG, WEBP o GIF.`;
  }
  if (file.size <= 0) {
    return `El archivo ${file.name} está vacío.`;
  }
  if (file.size > MAX_IMAGE_SIZE_BYTES) {
    return `El archivo ${file.name} supera el límite de 5 MB.`;
  }
  return null;
}

export const createAuctionSchema = z.object({
  title: z
    .string()
    .trim()
    .min(5, { message: 'El título debe tener al menos 5 caracteres' })
    .max(100, { message: 'El título no puede superar los 100 caracteres' }),
  description: z
    .string()
    .trim()
    .min(20, { message: 'La descripción debe tener al menos 20 caracteres' })
    .max(1000, { message: 'La descripción no puede superar los 1000 caracteres' }),
  category: z.string().min(1, { message: 'Selecciona una categoría' }),
  initialPrice: z.coerce
    .number()
    .positive({ message: 'El precio inicial debe ser mayor que 0' }),
  minBidIncrement: z.coerce
    .number()
    .positive({ message: 'El incremento mínimo debe ser mayor que 0' })
    .optional(),
  depositRequired: z.coerce
    .number()
    .min(0, { message: 'El depósito requerido no puede ser negativo' })
    .default(0),
  endDate: z
    .string()
    .min(1, { message: 'Selecciona una fecha de finalización' })
    .refine(
      (val) => {
        const parsed = new Date(val);
        if (Number.isNaN(parsed.getTime())) return false;
        return parsed.getTime() >= Date.now() + 60 * 60 * 1000;
      },
      { message: 'La fecha de finalización debe ser al menos 1 hora posterior al momento actual' }
    ),
  condition: z.string().min(1, { message: 'Selecciona el estado del artículo' }),
  shippingOptions: z.string().min(1, { message: 'Selecciona una opción de envío' }),
  allowPause: z.boolean().default(false),
  terms: z.boolean().refine((val) => val === true, {
    message: 'Debes aceptar los términos y condiciones',
  }),
});

export type CreateAuctionFormType = z.infer<typeof createAuctionSchema>;
