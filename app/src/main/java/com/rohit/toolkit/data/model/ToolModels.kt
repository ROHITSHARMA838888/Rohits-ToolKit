package com.rohit.toolkit.data.model

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.ui.graphics.vector.ImageVector

enum class ToolCategoryId(
    val title: String,
    val description: String,
    val icon: ImageVector
) {
    ORGANIZE_PDF("Organize PDF", "Merge, split, remove & organize PDF pages", Icons.Default.FolderZip),
    OPTIMIZE_PDF("Optimize PDF", "Compress, repair & OCR your PDFs", Icons.Default.Speed),
    CONVERT_PDF("Convert PDF", "Convert between PDF, Word, Excel, PPT, JPG & HTML", Icons.Default.Transform),
    EDIT_PDF("Edit PDF", "Rotate, page numbers, watermarks, crop & forms", Icons.Default.Edit),
    PDF_SECURITY("PDF Security", "Protect, unlock, sign, redact & compare PDFs", Icons.Default.Security),
    PDF_INTELLIGENCE("PDF Intelligence", "AI summarizer, translation & Markdown converter", Icons.Default.Psychology),
    IMAGE_TOOLS("Image Tools", "Compress, resize, edit, background removal & filters", Icons.Default.Image),
    ML_KIT("Google ML Kit Tools", "OCR, Barcode scanner, Face & Language detection", Icons.Default.SmartToy),
    FILE_MANAGER("File Manager", "Browse, rename, print, share & delete files", Icons.Default.Folder),
    MATHEMATICS("Mathematics", "GST, Percentage, Profit & Loss, Discount & Converters", Icons.Default.Calculate),
    ARCHIVE("Archive & Compression", "Create & extract ZIP archives securely", Icons.Default.Archive)
}

data class ToolItem(
    val id: String,
    val title: String,
    val description: String,
    val categoryId: ToolCategoryId,
    val icon: ImageVector,
    val route: String
)

object ToolRegistry {

    val allTools: List<ToolItem> = listOf(
        // CATEGORY 01: Organize PDF
        ToolItem("pdf_merge", "Merge PDF", "Combine multiple PDF documents into one ordered file", ToolCategoryId.ORGANIZE_PDF, Icons.Default.CallMerge, "tool/pdf_merge"),
        ToolItem("pdf_split", "Split PDF", "Split a PDF document by pages or extract page ranges", ToolCategoryId.ORGANIZE_PDF, Icons.Default.CallSplit, "tool/pdf_split"),
        ToolItem("pdf_remove_pages", "Remove Pages", "Delete unnecessary pages from your PDF file", ToolCategoryId.ORGANIZE_PDF, Icons.Default.DeleteSweep, "tool/pdf_remove_pages"),
        ToolItem("pdf_extract_pages", "Extract Pages", "Export selected individual pages into a separate PDF", ToolCategoryId.ORGANIZE_PDF, Icons.Default.FileOpen, "tool/pdf_extract_pages"),
        ToolItem("pdf_organize", "Organize PDF", "Reorder, rotate and arrange PDF pages visually", ToolCategoryId.ORGANIZE_PDF, Icons.Default.ViewAgenda, "tool/pdf_organize"),
        ToolItem("pdf_scan", "Scan to PDF", "Capture camera pages and convert into a multi-page PDF", ToolCategoryId.ORGANIZE_PDF, Icons.Default.DocumentScanner, "tool/pdf_scan"),

        // CATEGORY 02: Optimize PDF
        ToolItem("pdf_compress", "Compress PDF", "Reduce PDF file size while preserving readability", ToolCategoryId.OPTIMIZE_PDF, Icons.Default.Compress, "tool/pdf_compress"),
        ToolItem("pdf_repair", "Repair PDF", "Rebuild corrupted PDF document headers and xref tables", ToolCategoryId.OPTIMIZE_PDF, Icons.Default.Build, "tool/pdf_repair"),
        ToolItem("pdf_ocr", "OCR PDF", "Extract embedded text and recognize characters from pages", ToolCategoryId.OPTIMIZE_PDF, Icons.Default.TextSnippet, "tool/pdf_ocr"),

        // CATEGORY 03: Convert PDF
        ToolItem("convert_jpg_to_pdf", "JPG to PDF", "Convert images (JPG/PNG) into a single PDF document", ToolCategoryId.CONVERT_PDF, Icons.Default.PictureAsPdf, "tool/convert_jpg_to_pdf"),
        ToolItem("convert_word_to_pdf", "Word to PDF", "Convert Word documents (.docx) to PDF format", ToolCategoryId.CONVERT_PDF, Icons.Default.Description, "tool/convert_word_to_pdf"),
        ToolItem("convert_ppt_to_pdf", "PowerPoint to PDF", "Convert presentation slides (.pptx) to PDF format", ToolCategoryId.CONVERT_PDF, Icons.Default.Slideshow, "tool/convert_ppt_to_pdf"),
        ToolItem("convert_excel_to_pdf", "Excel to PDF", "Convert spreadsheet worksheets (.xlsx) to PDF format", ToolCategoryId.CONVERT_PDF, Icons.Default.TableChart, "tool/convert_excel_to_pdf"),
        ToolItem("convert_html_to_pdf", "HTML to PDF", "Render and convert web pages or HTML code to PDF", ToolCategoryId.CONVERT_PDF, Icons.Default.Html, "tool/convert_html_to_pdf"),
        ToolItem("convert_pdf_to_jpg", "PDF to JPG", "Extract high quality image files from each PDF page", ToolCategoryId.CONVERT_PDF, Icons.Default.Image, "tool/convert_pdf_to_jpg"),
        ToolItem("convert_pdf_to_word", "PDF to Word", "Extract structured text and formatting to Word .docx", ToolCategoryId.CONVERT_PDF, Icons.Default.Article, "tool/convert_pdf_to_word"),
        ToolItem("convert_pdf_to_ppt", "PDF to PowerPoint", "Convert PDF document pages to presentation slides", ToolCategoryId.CONVERT_PDF, Icons.Default.CoPresent, "tool/convert_pdf_to_ppt"),
        ToolItem("convert_pdf_to_excel", "PDF to Excel", "Extract tabular figures and text to Excel .xlsx", ToolCategoryId.CONVERT_PDF, Icons.Default.GridOn, "tool/convert_pdf_to_excel"),
        ToolItem("convert_pdf_to_pdfa", "PDF to PDF/A", "Convert standard PDF to archival ISO PDF/A compliance", ToolCategoryId.CONVERT_PDF, Icons.Default.Archive, "tool/convert_pdf_to_pdfa"),

        // CATEGORY 04: Edit PDF
        ToolItem("pdf_rotate", "Rotate PDF", "Rotate PDF pages clockwise 90°, 180°, or 270°", ToolCategoryId.EDIT_PDF, Icons.Default.RotateRight, "tool/pdf_rotate"),
        ToolItem("pdf_page_numbers", "Add Page Numbers", "Add custom numbered headers or footers to pages", ToolCategoryId.EDIT_PDF, Icons.Default.FormatListNumbered, "tool/pdf_page_numbers"),
        ToolItem("pdf_watermark", "Add Watermark", "Apply text or logo watermark with opacity control", ToolCategoryId.EDIT_PDF, Icons.Default.BrandingWatermark, "tool/pdf_watermark"),
        ToolItem("pdf_crop", "Crop PDF", "Crop PDF page margins and visible boundaries", ToolCategoryId.EDIT_PDF, Icons.Default.Crop, "tool/pdf_crop"),
        ToolItem("pdf_edit", "Edit PDF", "Add text annotations and freehand markings", ToolCategoryId.EDIT_PDF, Icons.Default.Edit, "tool/pdf_edit"),
        ToolItem("pdf_forms", "PDF Forms", "Fill, review, and flatten interactive AcroForm fields", ToolCategoryId.EDIT_PDF, Icons.Default.DynamicForm, "tool/pdf_forms"),

        // CATEGORY 05: PDF Security
        ToolItem("pdf_unlock", "Unlock PDF", "Remove document passwords with valid authorization", ToolCategoryId.PDF_SECURITY, Icons.Default.LockOpen, "tool/pdf_unlock"),
        ToolItem("pdf_protect", "Protect PDF", "Encrypt PDF with secure AES-128 / AES-256 passwords", ToolCategoryId.PDF_SECURITY, Icons.Default.Lock, "tool/pdf_protect"),
        ToolItem("pdf_sign", "Sign PDF", "Add visual drawn signatures and cryptographic timestamps", ToolCategoryId.PDF_SECURITY, Icons.Default.Draw, "tool/pdf_sign"),
        ToolItem("pdf_redact", "Redact PDF", "Permanently remove sensitive confidential text and areas", ToolCategoryId.PDF_SECURITY, Icons.Default.VisibilityOff, "tool/pdf_redact"),
        ToolItem("pdf_compare", "Compare PDF", "Compare two PDF documents side-by-side for differences", ToolCategoryId.PDF_SECURITY, Icons.Default.Compare, "tool/pdf_compare"),

        // CATEGORY 06: PDF Intelligence
        ToolItem("pdf_ai_summary", "AI Summarizer", "Analyze and summarize document key points locally", ToolCategoryId.PDF_INTELLIGENCE, Icons.Default.AutoAwesome, "tool/pdf_ai_summary"),
        ToolItem("pdf_translate", "Translate PDF", "Extract and translate document content into languages", ToolCategoryId.PDF_INTELLIGENCE, Icons.Default.Translate, "tool/pdf_translate"),
        ToolItem("pdf_to_markdown", "PDF to Markdown", "Convert PDF headings, paragraphs & tables to Markdown", ToolCategoryId.PDF_INTELLIGENCE, Icons.Default.Code, "tool/pdf_to_markdown"),

        // CATEGORY 07: Image Tools (13 tools)
        ToolItem("img_compress", "Compress Image", "Optimize image file size with quality percentage controls", ToolCategoryId.IMAGE_TOOLS, Icons.Default.Compress, "tool/img_compress"),
        ToolItem("img_resize", "Resize Image", "Change pixel dimensions with aspect ratio preservation", ToolCategoryId.IMAGE_TOOLS, Icons.Default.AspectRatio, "tool/img_resize"),
        ToolItem("img_crop", "Crop Image", "Crop photos with preset ratios (1:1, 16:9, 4:3) or freeform", ToolCategoryId.IMAGE_TOOLS, Icons.Default.Crop, "tool/img_crop"),
        ToolItem("img_to_jpg", "Convert to JPG", "Convert PNG, WEBP, or BMP images into JPG format", ToolCategoryId.IMAGE_TOOLS, Icons.Default.Image, "tool/img_to_jpg"),
        ToolItem("img_from_jpg", "Convert from JPG", "Convert JPG images to PNG, WEBP, or BMP format", ToolCategoryId.IMAGE_TOOLS, Icons.Default.Transform, "tool/img_from_jpg"),
        ToolItem("img_photo_editor", "Photo Editor", "Adjust brightness, contrast, saturation, sepia & filters", ToolCategoryId.IMAGE_TOOLS, Icons.Default.AutoFixHigh, "tool/img_photo_editor"),
        ToolItem("img_upscale", "Upscale Image", "Enhance resolution up to 4x using super-sampling filters", ToolCategoryId.IMAGE_TOOLS, Icons.Default.ZoomIn, "tool/img_upscale"),
        ToolItem("img_remove_bg", "Remove Background", "Isolate subjects and eliminate backgrounds cleanly", ToolCategoryId.IMAGE_TOOLS, Icons.Default.ContentCut, "tool/img_remove_bg"),
        ToolItem("img_watermark", "Watermark Image", "Apply customized copyright text or logo watermark", ToolCategoryId.IMAGE_TOOLS, Icons.Default.BrandingWatermark, "tool/img_watermark"),
        ToolItem("img_meme_gen", "Meme Generator", "Add top and bottom caption text with meme typography", ToolCategoryId.IMAGE_TOOLS, Icons.Default.Mood, "tool/img_meme_gen"),
        ToolItem("img_rotate", "Rotate Image", "Rotate clockwise, counter-clockwise, or flip horizontally/vertically", ToolCategoryId.IMAGE_TOOLS, Icons.Default.RotateRight, "tool/img_rotate"),
        ToolItem("img_html_to_image", "HTML to Image", "Render web layout or code and export as bitmap image", ToolCategoryId.IMAGE_TOOLS, Icons.Default.Html, "tool/img_html_to_image"),
        ToolItem("img_blur_face", "Blur Face", "Automatically detect faces and apply privacy blur", ToolCategoryId.IMAGE_TOOLS, Icons.Default.BlurOn, "tool/img_blur_face"),

        // CATEGORY 08: Google ML Kit Tools (6 tools)
        ToolItem("ml_text_ocr", "Text Recognition / OCR", "Offline bundled text recognition from camera or images", ToolCategoryId.ML_KIT, Icons.Default.DocumentScanner, "tool/ml_text_ocr"),
        ToolItem("ml_qr_barcode", "QR & Barcode Scanner", "Offline bundled scanner for QR codes, UPC, EAN, Code128", ToolCategoryId.ML_KIT, Icons.Default.QrCodeScanner, "tool/ml_qr_barcode"),
        ToolItem("ml_face_detect", "Face Detection", "Detect face landmarks, contours, and head orientation", ToolCategoryId.ML_KIT, Icons.Default.Face, "tool/ml_face_detect"),
        ToolItem("ml_translation", "Text Translation", "Translate text between 50+ languages with auto model delivery", ToolCategoryId.ML_KIT, Icons.Default.GTranslate, "tool/ml_translation"),
        ToolItem("ml_lang_id", "Language Identification", "Identify primary language from entered text snippet", ToolCategoryId.ML_KIT, Icons.Default.Language, "tool/ml_lang_id"),
        ToolItem("ml_doc_scanner", "Document Scanner", "Official ML Kit document camera scanner with edge correction", ToolCategoryId.ML_KIT, Icons.Default.CameraAlt, "tool/ml_doc_scanner"),

        // CATEGORY 09: File Manager
        ToolItem("file_manager", "File Manager", "Manage processed files, rename, delete, print & share", ToolCategoryId.FILE_MANAGER, Icons.Default.Folder, "tool/file_manager"),

        // CATEGORY 10: Mathematics (6 tools)
        ToolItem("math_gst", "GST Calculator", "Calculate CGST, SGST, IGST and inclusive/exclusive amounts", ToolCategoryId.MATHEMATICS, Icons.Default.ReceiptLong, "tool/math_gst"),
        ToolItem("math_percentage", "Percentage Calculator", "Compute percentages, marksheet totals, and grades", ToolCategoryId.MATHEMATICS, Icons.Default.Percent, "tool/math_percentage"),
        ToolItem("math_profit_loss", "Profit & Loss Calculator", "Calculate cost price, selling price, profit/loss percentage", ToolCategoryId.MATHEMATICS, Icons.Default.TrendingUp, "tool/math_profit_loss"),
        ToolItem("math_discount", "Discount Calculator", "Determine final sale price, discount amount, and savings", ToolCategoryId.MATHEMATICS, Icons.Default.Sell, "tool/math_discount"),
        ToolItem("math_ratio", "Ratio Calculator", "Solve proportions (A:B = C:D) and simplify fractions", ToolCategoryId.MATHEMATICS, Icons.Default.Functions, "tool/math_ratio"),
        ToolItem("math_converter", "Length & Unit Converter", "Convert length, weight, area, volume, temperature, and data", ToolCategoryId.MATHEMATICS, Icons.Default.Straighten, "tool/math_converter"),

        // CATEGORY 11: Archive & Compression
        ToolItem("archive_zip", "Create ZIP Archive", "Compress multiple files into a secure ZIP archive", ToolCategoryId.ARCHIVE, Icons.Default.FolderZip, "tool/archive_zip"),
        ToolItem("archive_extract", "Extract ZIP Archive", "Extract files safely with Zip Slip and bomb protection", ToolCategoryId.ARCHIVE, Icons.Default.Unarchive, "tool/archive_extract"),
        ToolItem("archive_inspect", "Inspect Archive", "View archive contents and file headers without extraction", ToolCategoryId.ARCHIVE, Icons.Default.FindInPage, "tool/archive_inspect")
    )

    fun getToolsForCategory(categoryId: ToolCategoryId): List<ToolItem> {
        return allTools.filter { it.categoryId == categoryId }
    }

    fun getToolById(id: String): ToolItem? {
        return allTools.find { it.id == id }
    }
}
