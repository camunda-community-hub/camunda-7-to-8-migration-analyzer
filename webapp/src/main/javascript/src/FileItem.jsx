import { Loading, Tooltip } from "@carbon/react";

import {
  Download,
  TrashCan,
  View,
  WarningFilled,
  CheckmarkFilled,
} from "@carbon/react/icons";

import Paperclip from "./Paperclip.svg";

export default function DropZone({
  name,
  error,
  status,
  isChecked,
  isConverted,
  downloadAction,
  previewAction,
  onDelete,
}) {
  return (
    <div className="FileItem">
      <div className="left">
        <img src={Paperclip} />
        <span
          className={downloadAction && !error ? "downloadable" : ""}
          onClick={error ? undefined : downloadAction}
        >
          {name}
        </span>
        {status === "success" && (
          <div style={{ color: "#2ada1e"}}>
            <CheckmarkFilled />
          </div>
        )}

      </div>
      <div className="right">

        {error && (
          <Tooltip label={error}>
            <div style={{ color: "#da1e28" }}>
              <WarningFilled />
            </div>
          </Tooltip>
        )}
        {status === "uploading" && !isChecked && <Loading small withOverlay={false} />}
        {isChecked && previewAction && (
          <button className="download" onClick={previewAction} title="Preview the analyzer results for this model">
            <View />
          </button>
        )}
        {status === "uploading" && !isConverted && <Loading small withOverlay={false} />}
        {isConverted && downloadAction && (
          <button className="download" onClick={downloadAction} title="Download the converted model">
            <Download />
          </button>
        )}
        {onDelete && (
          <button onClick={onDelete}>
            <TrashCan />
          </button>
        )}        

        {status === "error" && (
          <Tooltip label="File upload failure">
            <div style={{ color: "#da1e28" }}>
              <WarningFilled />
            </div>
          </Tooltip>
        )}
      </div>
    </div>
  );
}
