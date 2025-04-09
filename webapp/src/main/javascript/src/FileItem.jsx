import { Loading, Tooltip } from "@carbon/react";

import { Download, TrashCan, WarningFilled } from "@carbon/react/icons";

import Paperclip from "./Paperclip.svg";

export default function DropZone({
  name,
  error,
  status,
  downloadAction,
  onDelete,
}) {
  return (
    <div className="FileItem">
      <div className="left">
        <img src={Paperclip} />
        <span
          className={downloadAction ? "downloadable" : ""}
          onClick={downloadAction}
        >
          {name}
        </span>
      </div>
      <div className="right">
        {error && (
          <Tooltip label={error}>
            <div style={{ color: "#da1e28" }}>
              <WarningFilled />
            </div>
          </Tooltip>
        )}
        {!error && downloadAction && (
          <button className="download" onClick={downloadAction}>
            <Download />
          </button>
        )}
        {onDelete && (
          <button onClick={onDelete}>
            <TrashCan />
          </button>
        )}
        {status === "uploading" && <Loading small withOverlay={false} />}
      </div>
    </div>
  );
}
