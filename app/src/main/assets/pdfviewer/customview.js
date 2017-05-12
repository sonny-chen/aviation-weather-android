// Source: this page is adapted from: https://github.com/pauldmps/Android-pdf.js?files=1
PDFJS.disableWorker = false;

var pdfDoc = null,
    scale = 1.0;

/**
 * Get page info from document, resize canvas accordingly, and render page.
 * @param num Page number.
 */
function renderPage(canvas, num) {
  // Using promise to fetch the page
  pdfDoc.getPage(num).then(function(page) {
    var viewport = page.getViewport(scale);
    canvas.height = viewport.height;
    canvas.width = viewport.width;

    // Render PDF page into canvas context
    var renderContext = {
      canvasContext: canvas.getContext('2d'),
      viewport: viewport
    };
    var renderTask = page.render(renderContext);

    // Wait for rendering to finish
    renderTask.promise.then(function () {
        // render complete
        Android.pageReady(num)
    });
  });
}

/**
 * Asynchronously downloads PDF.
 */
PDFJS.getDocument({ url: url, password: pdf_password } ).then(function getPdf(pdfDoc_) {
    pdfDoc = pdfDoc_;
    Android.pageCount(pdfDoc.numPages);

    // page rendering
    for (i=1;i<=pdfDoc.numPages;i++)
    {
        var frame = document.getElementById('template').cloneNode(true);
        frame.id = "canvas_" + i;
        var canvas = document.getElementById('canvas');
        canvas.appendChild(frame);
        renderPage(frame, i);
    }
}, function getDocumentError(message) {
    Android.exception(message)
});
